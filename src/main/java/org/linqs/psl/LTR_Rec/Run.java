package org.linqs.psl.LTR_Rec;

import org.linqs.psl.LTR_Rec.models.RankingPSLModel;

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
import java.util.Map;
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
    private RankingPSLModel model;
    private String datasetName;
    private String dataSubPath;
    private Properties configs;

    public Run(String dsName, String ablationSetting, String dataPath) {
        try {
            configs = readPropertiesFile(DATA_SOURCE_CONFIG_FILE_NAME);
        } catch (IOException ioe){
            log.info(ioe.getMessage());
            System.exit(1);
        }

        // String suffix = System.getProperty("user.name") + "@" + getHostname();
        // String baseDBPath = Config.getString("dbpath", System.getProperty("java.io.tmpdir"));
        // String dbPath = Paths.get(baseDBPath, this.getClass().getName() + "_" + suffix).toString();
        // dataStore = new RDBMSDataStore(new H2DatabaseDriver(Type.Disk, dbPath, true));
        dataStore = new RDBMSDataStore(new PostgreSQLDriver("psl", true));

        model = new RankingPSLModel(ablationSetting, dsName, dataStore);

        datasetName = dsName;
        dataSubPath = dataPath;
    }

    private Properties readPropertiesFile(String fileName) throws IOException {
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
        // Default Predicates
        model.addDefaultPredicates();

        // Add Ablation Setting Predicates
        model.addAblationSettingPredicates();
    }

    /**
     * Defines the rules for this model.
     */
    private void defineRules() {
        log.info("Defining model rules");

        // Default rules
        model.addDefaultRules();

        // Ablation Setting Rules
        model.addAblationSettingRules();

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

        Inserter inserter;
        // Observed
        for(Map.Entry ObservedPredicateData: model.getDefaultObservedPredicateData().entrySet()){
            inserter = dataStore.getInserter(model.getStandardPredicate((String)ObservedPredicateData.getKey()), obsPartition);
            inserter.loadDelimitedDataAutomatic(Paths.get(DATA_PATH, datasetName,
                    dataSubPath, ObservedPredicateData.getValue() + "_obs.txt").toString());
        }

        for(Map.Entry ObservedPredicateData: model.getAblationObservedPredicateData().entrySet()){
            inserter = dataStore.getInserter(model.getStandardPredicate((String)ObservedPredicateData.getKey()), obsPartition);
            inserter.loadDelimitedDataAutomatic(Paths.get(DATA_PATH, datasetName, dataSubPath,
                    configs.getProperty(datasetName + ObservedPredicateData.getValue()) + "_obs.txt").toString());
        }

        // Targets
        for(Map.Entry TargetsPredicateData: model.getDefaultTargetPredicateData().entrySet()){
            inserter = dataStore.getInserter(model.getStandardPredicate((String)TargetsPredicateData.getKey()), targetsPartition);
            inserter.loadDelimitedDataAutomatic(Paths.get(DATA_PATH, datasetName,
                    dataSubPath, TargetsPredicateData.getValue() + "_targets.txt").toString());
        }

        for(Map.Entry TargetsPredicateData: model.getAblationTargetPredicateData().entrySet()){
            inserter = dataStore.getInserter(model.getStandardPredicate((String)TargetsPredicateData.getKey()), targetsPartition);
            inserter.loadDelimitedDataAutomatic(Paths.get(DATA_PATH, datasetName, dataSubPath,
                    configs.getProperty(datasetName + TargetsPredicateData.getValue()) + "_targets.txt").toString());
        }

        // Truths
        for(Map.Entry TruthPredicateData: model.getDefaultTruthPredicateData().entrySet()){
            inserter = dataStore.getInserter(model.getStandardPredicate((String)TruthPredicateData.getKey()), truthPartition);
            inserter.loadDelimitedDataAutomatic(Paths.get(DATA_PATH, datasetName,
                    dataSubPath, TruthPredicateData.getValue() + "_truth.txt").toString());
        }

        for(Map.Entry TruthPredicateData: model.getAblationTruthPredicateData().entrySet()){
            inserter = dataStore.getInserter(model.getStandardPredicate((String)TruthPredicateData.getKey()), truthPartition);
            inserter.loadDelimitedDataAutomatic(Paths.get(DATA_PATH, datasetName, dataSubPath,
                    configs.getProperty(datasetName + TruthPredicateData.getValue()) + "_truth.txt").toString());
        }

    }

    /**
     * Run inference to infer the unknown Preferences and Rankings.
     */
    private void runInference(Partition obsPartition, Partition targetsPartition) {
        log.info("Starting inference");

        StandardPredicate[] DefaultClosedPredicates = model.getDefaultClosedPredicates();
        StandardPredicate[] AblationClosedPredicates = model.getAblationClosedPredicates();
        StandardPredicate[] closedPredicates = new StandardPredicate[DefaultClosedPredicates.length +
                AblationClosedPredicates.length];
        System.arraycopy(DefaultClosedPredicates, 0, closedPredicates,
                0, DefaultClosedPredicates.length);
        System.arraycopy(AblationClosedPredicates, 0, closedPredicates,
                DefaultClosedPredicates.length, AblationClosedPredicates.length);

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

        for(String OpenPredicateName: model.getAblationOpenPredicateNames()){
            FileWriter pref_writer = new FileWriter(Paths.get(OUTPUT_PATH, datasetName,
                    OpenPredicateName + ".txt").toString());

            for (GroundAtom atom : resultsDB.getAllGroundAtoms(model.getStandardPredicate(OpenPredicateName))) {
                for (Constant argument : atom.getArguments()) {
                    pref_writer.write(argument.toString() + "\t");
                }
                pref_writer.write("" + atom.getValue() + "\n");
            }

            pref_writer.close();
        }

        FileWriter rank_writer = new FileWriter(Paths.get(OUTPUT_PATH, datasetName, "Ranking.txt").toString());

        for (GroundAtom atom : resultsDB.getAllGroundAtoms(
                model.getStandardPredicate(configs.getProperty(datasetName + "_RankPredicate")))) {
            for (Constant argument : atom.getArguments()) {
                rank_writer.write(argument.toString() + "\t");
            }
            rank_writer.write("" + atom.getValue() + "\n");
        }

        rank_writer.close();
        resultsDB.close();
    }

    /**
     * Run statistical evaluation scripts to determine the quality of the inferences
     * relative to the defined truth.
     */
    private void evalResults(Partition targetsPartition, Partition truthPartition) {

        StandardPredicate[] DefaultClosedPredicates = model.getDefaultClosedPredicates();
        StandardPredicate[] AblationClosedPredicates = model.getAblationClosedPredicates();
        StandardPredicate[] closedPredicates = new StandardPredicate[DefaultClosedPredicates.length +
                AblationClosedPredicates.length];
        System.arraycopy(DefaultClosedPredicates, 0, closedPredicates,
                0, DefaultClosedPredicates.length);
        System.arraycopy(AblationClosedPredicates, 0, closedPredicates,
                DefaultClosedPredicates.length, AblationClosedPredicates.length);

        Database resultsDB = dataStore.getDatabase(targetsPartition, closedPredicates);
        Database truthDB = dataStore.getDatabase(truthPartition,
                new StandardPredicate[]{
                        model.getStandardPredicate(configs.getProperty(datasetName + "_RankPredicate"))
        });

        PersistedAtomManager atomManager = new PersistedAtomManager(resultsDB, false);
        TrainingMap trainingMap = new TrainingMap(atomManager, truthDB, true, false);

        // Using default threshold for Relevance: 0.5
        RankingEvaluator eval = new RankingEvaluator();
        eval.compute(trainingMap, model.getStandardPredicate(configs.getProperty(datasetName + "_RankPredicate")));
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
        String ablationSetting = args[1];
        String dataPath = args[2];

        Run run = new Run(datasetName, ablationSetting, dataPath);
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
