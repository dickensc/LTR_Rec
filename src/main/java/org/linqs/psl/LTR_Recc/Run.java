package org.linqs.psl.LTR_Recc;

import org.linqs.psl.application.inference.InferenceApplication;
import org.linqs.psl.application.inference.MPEInference;
import org.linqs.psl.application.learning.weight.TrainingMap;
import org.linqs.psl.config.Config;
import org.linqs.psl.database.atom.PersistedAtomManager;
import org.linqs.psl.database.Database;
import org.linqs.psl.database.DataStore;
import org.linqs.psl.database.Partition;
import org.linqs.psl.database.loading.Inserter;
import org.linqs.psl.database.rdbms.driver.H2DatabaseDriver;
import org.linqs.psl.database.rdbms.driver.H2DatabaseDriver.Type;
import org.linqs.psl.database.rdbms.driver.PostgreSQLDriver;
import org.linqs.psl.database.rdbms.RDBMSDataStore;
import org.linqs.psl.evaluation.statistics.RankingEvaluator;
import org.linqs.psl.evaluation.statistics.Evaluator;
import org.linqs.psl.java.PSLModel;
import org.linqs.psl.model.atom.GroundAtom;
import org.linqs.psl.model.predicate.StandardPredicate;
import org.linqs.psl.model.term.Constant;
import org.linqs.psl.model.term.ConstantType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;

/**
 */
public class Run {
    private static final String PARTITION_OBSERVATIONS = "observations";
    private static final String PARTITION_TARGETS = "targets";
    private static final String PARTITION_TRUTH = "truth";

    private static final String DATA_PATH = Paths.get(".", "data").toString();
    private static final String OUTPUT_PATH = "inferred-predicates";

    private static final String CONFIG_PATH = "src/main/resources";
    private static final String DATA_SOURCE_CONFIG_FILE_NAME = "dataSourceConfig.properties";

    private static Logger log = LoggerFactory.getLogger(Run.class);

    private DataStore dataStore;
    private PSLModel model;
    private String datasetName;
    private String dataSubPath;
    private Properties configs;

    public Run(String dsName, String dataPath) {
        try {
            configs = readPropertiesFile(DATA_SOURCE_CONFIG_FILE_NAME);
        } catch (IOException ioe){
            log.info(ioe.getMessage());
            System.exit(1);
        }

        String suffix = System.getProperty("user.name") + "@" + getHostname();
        String baseDBPath = Config.getString("dbpath", System.getProperty("java.io.tmpdir"));
        String dbPath = Paths.get(baseDBPath, this.getClass().getName() + "_" + suffix).toString();
        dataStore = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbPath, true));
        // dataStore = new RDBMSDataStore(new PostgreSQLDriver("psl", true));

        model = new PSLModel(dataStore);

        datasetName = dsName;
        dataSubPath = dataPath;
    }

    private Properties readPropertiesFile(String fileName) throws IOException{
        log.info("Loading Configurations");

        FileInputStream fileInputStream = null;
        Properties configs = new Properties();
        try {
            fileInputStream = new FileInputStream(Paths.get(CONFIG_PATH, fileName).toString());
            configs.load(fileInputStream);
        } catch(FileNotFoundException exception) {
            log.info(exception.getMessage());
            System.exit(1);
        } finally {
            if (fileInputStream != null){
                fileInputStream.close();
            }
        }
        return configs;
    }

    /**
     * Defines the logical predicates used in this model.
     */
    private void definePredicates() {
        model.addPredicate("Preference", ConstantType.UniqueStringID, ConstantType.UniqueStringID);
        model.addPredicate("RelativeRank", ConstantType.UniqueStringID, ConstantType.UniqueStringID, ConstantType.UniqueStringID);
        model.addPredicate("SimilarUsers", ConstantType.UniqueStringID, ConstantType.UniqueStringID);
        model.addPredicate("SimilarItems", ConstantType.UniqueStringID, ConstantType.UniqueStringID);
    }

    /**
     * Defines the rules for this model.
     */
    private void defineRules() {
        log.info("Defining model rules");

        // point-wise predictions
        model.addRule("1: Preference(U1, I1) & SimilarUsers(U1, U2) -> Preference(U2, I1) ^2");
        model.addRule("1: Preference(U1, I1) & SimilarItems(I1, I2) -> Preference(U1, I2) ^2");

        // pair-wise predictions
        model.addRule("RelativeRank(U1, I1, I2) + RelativeRank(U1, I2, I1) = 1.");
        model.addRule("1: RelativeRank(U1, I1, I2) & SimilarUsers(U1, U2) -> RelativeRank(U2, I1, I2) ^2");
        model.addRule("1: RelativeRank(U1, I1, I2) & SimilarItems(I1, I3) -> RelativeRank(U1, I1, I3) ^2");

        // pair-wise and point-wise combination
        model.addRule("1: 0.5 * Preference(U1, I1) - 0.5 * Preference(U1, I2) + 0.5 <= RelativeRank(U1, I1, I2) ^2");

        log.debug("model: {}", model);
    }

    /**
     * Load data from text files into the DataStore.
     * Three partitions are defined and populated: observations, targets, and truth.
     * Observations contains evidence that we treat as background knowledge and use to condition our inferences.
     * Targets contains the inference targets - the unknown variables we wish to infer.
     * Truth contains the true values of the inference variables and will be used to evaluate the model's performance.
     */
    private void loadData(Partition obsPartition, Partition targetsPartition, Partition truthPartition) {
        log.info("Loading data into database");

        String path = FileSystems.getDefault().getPath(".").toAbsolutePath().toString();
        System.out.println("Current relative path is: " + path);

        Inserter inserter = dataStore.getInserter(model.getStandardPredicate("RelativeRank"), obsPartition);
        inserter.loadDelimitedDataTruth(Paths.get(DATA_PATH, datasetName,
                dataSubPath, configs.getProperty(datasetName + "_rel_rank") + "_obs.txt").toString());

        inserter = dataStore.getInserter(model.getStandardPredicate("Preference"), obsPartition);
        inserter.loadDelimitedDataTruth(Paths.get(DATA_PATH, datasetName,
                dataSubPath, configs.getProperty(datasetName + "_pref") + "_obs.txt").toString());

        inserter = dataStore.getInserter(model.getStandardPredicate("SimilarUsers"), obsPartition);
        inserter.loadDelimitedDataTruth(Paths.get(DATA_PATH, datasetName,
                dataSubPath, configs.getProperty(datasetName + "_sim_users") + "_obs.txt").toString());

        inserter = dataStore.getInserter(model.getStandardPredicate("SimilarItems"), obsPartition);
        inserter.loadDelimitedDataTruth(Paths.get(DATA_PATH, datasetName,
                dataSubPath, configs.getProperty(datasetName + "_sim_items") + "_obs.txt").toString());

        inserter = dataStore.getInserter(model.getStandardPredicate("Preference"), targetsPartition);
        inserter.loadDelimitedData(Paths.get(DATA_PATH, datasetName,
                dataSubPath, configs.getProperty(datasetName + "_pref") + "_targets.txt").toString());


        inserter = dataStore.getInserter(model.getStandardPredicate("RelativeRank"), targetsPartition);
        inserter.loadDelimitedData(Paths.get(DATA_PATH, datasetName,
                dataSubPath, configs.getProperty(datasetName + "_rel_rank") + "_targets.txt").toString());

        inserter = dataStore.getInserter(model.getStandardPredicate("Preference"), truthPartition);
        inserter.loadDelimitedDataTruth(Paths.get(DATA_PATH, datasetName,
                dataSubPath, configs.getProperty(datasetName + "_pref") + "_truth.txt").toString());

        inserter = dataStore.getInserter(model.getStandardPredicate("RelativeRank"), truthPartition);
        inserter.loadDelimitedDataTruth(Paths.get(DATA_PATH, datasetName,
                dataSubPath, configs.getProperty(datasetName + "_rel_rank") + "_truth.txt").toString());

    }

    /**
     * Run inference to infer the unknown RelativeRanks and Preferences.
     */
    private void runInference(Partition obsPartition, Partition targetsPartition) {
        log.info("Starting inference");

        StandardPredicate[] closedPredicates = new StandardPredicate[]{model.getStandardPredicate("SimilarUsers"),
                model.getStandardPredicate("SimilarItems")};
        Database inferDB = dataStore.getDatabase(targetsPartition, closedPredicates, obsPartition);

        InferenceApplication inference = new MPEInference(model, inferDB);
        inference.inference();

        inference.close();
        inferDB.close();

        log.info("Inference complete");
    }

    /**
     * Writes the output of the model into a file.
     */
    private void writeOutput(Partition targetsPartition) throws IOException {
        Database resultsDB = dataStore.getDatabase(targetsPartition);

        (new File(OUTPUT_PATH)).mkdirs();
        (new File(Paths.get(OUTPUT_PATH, datasetName).toString())).mkdirs();
        FileWriter writer = new FileWriter(Paths.get(OUTPUT_PATH, datasetName,"RelativeRank.txt").toString());

        for (GroundAtom atom : resultsDB.getAllGroundAtoms(model.getStandardPredicate("RelativeRank"))) {
            for (Constant argument : atom.getArguments()) {
                writer.write(argument.toString() + "\t");
            }
            writer.write("" + atom.getValue() + "\n");
        }
        writer.close();

        writer = new FileWriter(Paths.get(OUTPUT_PATH, datasetName, "Preference.txt").toString());

        for (GroundAtom atom : resultsDB.getAllGroundAtoms(model.getStandardPredicate("Preference"))) {
            for (Constant argument : atom.getArguments()) {
                writer.write(argument.toString() + "\t");
            }
            writer.write("" + atom.getValue() + "\n");
        }

        writer.close();
        resultsDB.close();
    }

    /**
     * Run statistical evaluation scripts to determine the quality of the inferences
     * relative to the defined truth.
     */
    private void evalResults(Partition targetsPartition, Partition truthPartition) {
        StandardPredicate[] closedPredicates = new StandardPredicate[]{model.getStandardPredicate("SimilarUsers"),
                model.getStandardPredicate("SimilarItems")};
        Database resultsDB = dataStore.getDatabase(targetsPartition, closedPredicates);
        Database truthDB = dataStore.getDatabase(truthPartition,
                new StandardPredicate[]{model.getStandardPredicate("Preference")});

        PersistedAtomManager atomManager = new PersistedAtomManager(resultsDB, false);
        TrainingMap trainingMap = new TrainingMap(atomManager, truthDB, true, false);

        // Using default threshold for relevance: 0.5
        RankingEvaluator eval = new RankingEvaluator();
        eval.compute(trainingMap, model.getStandardPredicate("Preference"));
        log.info(eval.getAllStats());

        resultsDB.close();
        truthDB.close();
    }

    public void run() {
        Partition obsPartition = dataStore.getPartition(PARTITION_OBSERVATIONS);
        Partition targetsPartition = dataStore.getPartition(PARTITION_TARGETS);
        Partition truthPartition = dataStore.getPartition(PARTITION_TRUTH);

        definePredicates();
        defineRules();
        loadData(obsPartition, targetsPartition, truthPartition);

        runInference(obsPartition, targetsPartition);
        try {
            writeOutput(targetsPartition);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to write out results.", ex);
        }
        evalResults(targetsPartition, truthPartition);

        dataStore.close();
    }

    /**
     * Run this model from the command line.
     */
    public static void main(String[] args) {
        String datasetName = args[0];
        String dataPath = args[1];

        Run run = new Run(datasetName, dataPath);
        run.run();
    }

    private static String getHostname() {
        String hostname = "unknown";

        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            log.warn("Hostname can not be resolved, using '" + hostname + "'.");
        }

        return hostname;
    }
}
