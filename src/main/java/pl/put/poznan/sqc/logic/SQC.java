package pl.put.poznan.sqc.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pl.put.poznan.sqc.logic.visitor.KeywordStepCountVisitor;
import pl.put.poznan.sqc.logic.visitor.LimitedScenarioVisitor;
import pl.put.poznan.sqc.logic.visitor.StepsWithoutActorsVisitor;
import pl.put.poznan.sqc.logic.visitor.TextualScenarioVisitor;
import pl.put.poznan.sqc.logic.visitor.TotalStepCountVisitor;
import pl.put.poznan.sqc.model.*;

import java.util.*;

/**
 * SQC (Scenario Quality Checker) Service.
 * 
 * Core business logic for analyzing and validating functional requirements written in scenario format.
 * Provides automated feedback on scenarios including metrics, error detection, and scenario transformation.
 * 
 * <p>Key responsibilities:
 * <ul>
 *   <li>Normalize and validate scenario structure and content</li>
 *   <li>Extract and analyze scenario components (keywords, actors, steps)</li>
 *   <li>Generate quantitative metrics (step counts, keyword analysis)</li>
 *   <li>Identify quality issues (missing actors, invalid steps)</li>
 *   <li>Transform scenarios (depth limiting, textual representation)</li>
 * </ul>
 * 
 * <p>Recognized flow control keywords: IF, ELSE, FOR EACH
 * 
 * @author Scenario Quality Checker Team
 * @version 1.0
 */
@Service
public class SQC {
    private static final Logger logger = LoggerFactory.getLogger(SQC.class);
    private static final Set<String> keywords = new HashSet<>(Arrays.asList("IF", "ELSE", "FOR EACH"));

    /**
     * Analyzes a scenario and returns results as a textual scenario representation.
     * 
     * <p>This method performs normalization, validation, and generates a formatted text representation
     * of the scenario respecting the configured depth limit.
     * 
     * @param scenarioWrapper the scenario wrapper containing the scenario and analysis options
     * @return a string representation of the scenario (formatted with numbering and indentation),
     *         or null if the scenario is invalid or analysis fails
     */
    public String analyzeToText(ScenarioWrapper scenarioWrapper) {
        logger.info("Starting text scenario analysis");
        logger.debug("Text analysis input: {}", scenarioWrapper);

        List<String> warnings = new ArrayList<>();

        // 1. Normalization & Validation Phase
        AnalysisResponse normalizationResponse = normalize(scenarioWrapper, warnings);
        if (normalizationResponse.getStatus() != ResponseStatus.SUCCESS) {
            logger.info("Text scenario analysis failed during normalization with status {}", normalizationResponse.getStatus());
            return null;
        }

        Scenario scenario = scenarioWrapper.scenario();
        Step rootStep = scenario.rootStep();
        int maxDepth = scenarioWrapper.options().maxDepth();
        logger.debug("Generating text scenario with maxDepth {}", maxDepth);

        List<String> textLines = generateTextualScenario(rootStep, maxDepth);
        if (textLines == null || textLines.isEmpty()) {
            logger.info("Text scenario analysis produced no text lines");
            return null;
        }

        logger.info("Text scenario analysis completed successfully");
        logger.debug("Text scenario warnings: {}", warnings);
        return String.join("\n", textLines);
    }

    /**
     * Analyzes a scenario and returns comprehensive analysis results.
     * 
     * <p>This method performs a complete analysis of the scenario including normalization, validation,
     * and computation of metrics/analysis based on the provided options. Results are returned in a
     * structured {@link AnalysisResponse} object.
     * 
     * <p>Analysis options control which computations are performed:
     * <ul>
     *   <li>{@code includeTotalStepCount} - Count all steps in the scenario</li>
     *   <li>{@code includeKeywordStepCount} - Count steps with flow control keywords</li>
     *   <li>{@code includeStepsWithoutActors} - Identify steps lacking an assigned actor</li>
     *   <li>{@code includeNumberedScenario} - Generate textual numbered representation</li>
     *   <li>{@code includeLimitedScenario} - Create scenario restricted to specified depth</li>
     *   <li>{@code maxDepth} - Maximum nesting depth (0 = unlimited)</li>
     * </ul>
     * 
     * @param scenarioWrapper the scenario wrapper containing the scenario and analysis options
     * @return an {@link AnalysisResponse} with status, metrics, and analysis results.
     *         Returns error status if the scenario is invalid or required actors are missing.
     *         Includes warnings for detected issues during normalization.
     */
    public AnalysisResponse analyze(ScenarioWrapper scenarioWrapper) {
        logger.info("Starting scenario analysis");
        logger.debug("Analysis input: {}", scenarioWrapper);

        List<String> warnings = new ArrayList<>();

        // 1. Normalization & Validation Phase
        AnalysisResponse normalizationResponse = normalize(scenarioWrapper, warnings);
        if (normalizationResponse.getStatus() != ResponseStatus.SUCCESS) {
            logger.info("Scenario analysis failed during normalization with status {}", normalizationResponse.getStatus());
            logger.debug("Scenario analysis warnings before failure: {}", warnings);
            return normalizationResponse;
        }

        Scenario scenario = scenarioWrapper.scenario();
        ScenarioOptions options = scenarioWrapper.options();
        Step rootStep = scenario.rootStep();
        logger.debug("Scenario analysis options: {}", options);

        // 2. Initialize the Builder (Base state)
        AnalysisResponse.Builder responseBuilder = AnalysisResponse.builder()
                .status(ResponseStatus.SUCCESS)
                .message("Analysis successfully completed");

        // 3. Conditional Computation Phase (The options logic)
        // Perform computations according to flags. If flag is set to false, return null on place of answer
        // Check for errors like ELSE without IF or nested block without keyword
        // I think that it should be allowed for a step with keyword to have no children
        if (options.includeTotalStepCount()) {
            responseBuilder.totalStepCount(countTotalSteps(rootStep));
        }

        if (options.includeKeywordStepCount()) {
            responseBuilder.keywordStepCount(countKeywordSteps(rootStep));
        }

        if (options.includeStepsWithoutActors()) {
            List<String> stepsWithoutActors = findStepsWithoutActors(rootStep);
            responseBuilder.stepsWithoutActors(stepsWithoutActors);
        }

        if (options.includeNumberedScenario()) {
            List<String> textualScenario = generateTextualScenario(rootStep, options.maxDepth());
            responseBuilder.textualScenario(textualScenario);
        }

        if (options.includeLimitedScenario()) {
            Scenario limitedScenario = generateLimitedScenario(scenario, options.maxDepth(), warnings);
            responseBuilder.limitedScenario(limitedScenario);
        }

        // 4. Build and return the final immutable object!
        AnalysisResponse response = responseBuilder.warnings(warnings).build();
        logger.info("Scenario analysis completed successfully");
        logger.debug("Scenario analysis warnings: {}", warnings);
        logger.debug("Scenario analysis response: {}", response);
        return response;
    }

    private AnalysisResponse normalize(ScenarioWrapper scenarioWrapper, List<String> warnings) {
        logger.debug("Normalizing scenario");
        Scenario scenario = scenarioWrapper.scenario();

        Set<String> validActorsSet = new HashSet<>();
        List<String> rawActors = new ArrayList<>();
        rawActors.addAll(scenario.externalActors());
        rawActors.addAll(scenario.systemActors());
        for (String actor : rawActors) {
            if (actor == null) {
                warnings.add("Encountered an empty or blank actor name. It was ignored.");
            } else {
                validActorsSet.add(actor);
            }
        }
        if(validActorsSet.isEmpty()) {
            logger.info("Scenario normalization failed because no actors were provided");
            return AnalysisResponse.builder().status(ResponseStatus.INPUT_ERROR).message("No actors provided").build();
        }
        List<String> validActors = new ArrayList<>(validActorsSet);
        validActors.sort((a, b) -> Integer.compare(b.length(), a.length()));

        if(scenario.title() == null) {
            warnings.add("No title provided");
        }

        Step rootStep = scenario.rootStep();
        if(rootStep.getText() != null) {
            warnings.add("Root step should not have text, only substeps");
        }

        checkSteps(rootStep.getSubSteps(), rootStep, validActors, warnings);
        logger.debug("Scenario normalization completed with warnings: {}", warnings);

        return AnalysisResponse.builder()
                .status(ResponseStatus.SUCCESS)
                .build();
    }

    private void checkSteps(List<Step> subSteps, Step parentStep, List<String> validActors, List<String> warnings) {
        Iterator<Step> iterator = subSteps.iterator();

        int i=1;
        while(iterator.hasNext()) {
            Step child = iterator.next();

            child.setOrderNumber(parentStep.getOrderNumber()+i+".");

            if(child.getText() == null && child.getSubSteps().isEmpty()) {
                warnings.add("Removed empty step with no text and children at position "+child.getOrderNumber());
                iterator.remove();
                continue;
            }

            if(child.getText() == null) {
                warnings.add("Step "+child.getOrderNumber()+" has no text");
            } else {
                extractStepFields(child, validActors, warnings);
            }

            if(!child.getSubSteps().isEmpty() && child.getKeyword() == null) {
                warnings.add("Step "+child.getOrderNumber()+" has no keyword, but has children");
            }
            checkSteps(child.getSubSteps(), child, validActors, warnings);

            i++;
        }


    }

    private void extractStepFields(Step step, List<String> validActors, List<String> warnings) {
        String remainingText = step.getText();

        for(String kw : keywords) {
            if(remainingText.toUpperCase().startsWith(kw)) {
                if(remainingText.length() == kw.length()) {
                    step.setKeyword(kw);
                    warnings.add("Step "+step.getOrderNumber()+" consists only of keyword");
                    return;
                }
                if(!Character.isLetterOrDigit(remainingText.charAt(kw.length()))) {
                    step.setKeyword(kw);
                    remainingText = remainingText.substring(kw.length()).replaceFirst("^[\\p{Punct}\\s]+", "");
                    if(remainingText.isEmpty()){
                        warnings.add("Step "+step.getOrderNumber()+" consists only of keyword");
                        return;
                    }
                    break;
                }
            }
        }

        for(String actor : validActors) {
            if(remainingText.toUpperCase().startsWith(actor.toUpperCase())) {
                if(remainingText.length() == actor.length()) {
                    step.setActor(actor);
                    if(step.getKeyword() == null)
                        warnings.add("Step "+step.getOrderNumber()+" consists only of actor");
                    else
                        warnings.add("Step "+step.getOrderNumber()+" consists only of actor and keyword");
                    return;
                }
                if(!Character.isLetterOrDigit(remainingText.charAt(actor.length()))) {
                    step.setActor(actor);
                    remainingText = remainingText.substring(actor.length()).replaceFirst("^[\\p{Punct}\\s]+", "");
                    if(remainingText.isEmpty()){
                        if(step.getKeyword() == null)
                            warnings.add("Step "+step.getOrderNumber()+" consists only of actor");
                        else
                            warnings.add("Step "+step.getOrderNumber()+" consists only of actor and keyword");
                        return;
                    }
                    break;
                }
            }
        }

        step.setCleanText(remainingText);
    }

    /**
     * Counts all steps in the scenario tree (excluding the invisible root step).
     */
    private Integer countTotalSteps(Step rootStep) {
        TotalStepCountVisitor visitor = new TotalStepCountVisitor();
        rootStep.accept(visitor);
        return visitor.getCount();
    }

    /**
     * Counts only steps that begin with a specific keyword (IF, ELSE, FOR EACH).
     */
    private Integer countKeywordSteps(Step rootStep) {
        KeywordStepCountVisitor visitor = new KeywordStepCountVisitor();
        rootStep.accept(visitor);
        return visitor.getCount();
    }

    /**
     * Returns a list of order numbers (e.g., ["1.2.", "2."]) for steps that lack an actor.
     */
    private List<String> findStepsWithoutActors(Step rootStep) {
        StepsWithoutActorsVisitor visitor = new StepsWithoutActorsVisitor();
        rootStep.accept(visitor);
        return visitor.getStepsWithoutActors();
    }

    /**
     * Reconstructs the scenario as a list of strings, restricted to a certain depth.
     * If maxDepth is 0, it means "no limit".
     * Format: "1.2. IF: Librarian adds book" with indentation for nesting.
     */
    private List<String> generateTextualScenario(Step rootStep, int maxDepth) {
        TextualScenarioVisitor visitor = new TextualScenarioVisitor(maxDepth);
        rootStep.accept(visitor);
        return visitor.getTextualScenario();
    }

    /**
     * Returns a copy of the scenario containing only steps up to maxDepth.
     * The invisible root step is depth 0, so maxDepth 1 means top-level steps only.
     * If maxDepth is 0, no limit is applied.
     */
    private Scenario generateLimitedScenario(Scenario scenario, int maxDepth, List<String> warnings) {
        if (maxDepth < 0) {
            warnings.add("maxDepth cannot be negative. Full scenario was returned.");
            maxDepth = 0;
        }

        LimitedScenarioVisitor visitor = new LimitedScenarioVisitor(maxDepth);
        scenario.rootStep().accept(visitor);

        return new Scenario(
                scenario.title(),
                scenario.externalActors(),
                scenario.systemActors(),
                visitor.getRootStep()
        );
    }
}
