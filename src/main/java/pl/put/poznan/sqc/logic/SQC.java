package pl.put.poznan.sqc.logic;

import org.springframework.stereotype.Service;
import pl.put.poznan.sqc.model.*;

import java.util.*;

@Service
public class SQC {
    private static final Set<String> keywords = new HashSet<>(Arrays.asList("IF", "ELSE", "FOR EACH"));

    public String analyzeToText(ScenarioWrapper scenarioWrapper) {
        List<String> warnings = new ArrayList<>();

        // 1. Normalization & Validation Phase
        AnalysisResponse normalizationResponse = normalize(scenarioWrapper, warnings);
        if (normalizationResponse.getStatus() != ResponseStatus.SUCCESS) {
            return null;
        }

        Scenario scenario = scenarioWrapper.scenario();
        Step rootStep = scenario.rootStep();
        int maxDepth = scenarioWrapper.options().maxDepth();

        List<String> textLines = generateTextualScenario(rootStep, maxDepth, warnings);
        if (textLines == null || textLines.isEmpty()) {
            return null;
        }

        return String.join("\n", textLines);
    }

    public AnalysisResponse analyze(ScenarioWrapper scenarioWrapper) {
        List<String> warnings = new ArrayList<>();

        // 1. Normalization & Validation Phase
        AnalysisResponse normalizationResponse = normalize(scenarioWrapper, warnings);
        if (normalizationResponse.getStatus() != ResponseStatus.SUCCESS) {
            return normalizationResponse;
        }

        Scenario scenario = scenarioWrapper.scenario();
        ScenarioOptions options = scenarioWrapper.options();
        Step rootStep = scenario.rootStep();

        // 2. Initialize the Builder (Base state)
        AnalysisResponse.Builder responseBuilder = AnalysisResponse.builder()
                .status(ResponseStatus.SUCCESS)
                .message("Analysis successfully completed");

        // 3. Conditional Computation Phase (The options logic)
        // Perform computations according to flags. If flag is set to false, return null on place of answer
        // Check for errors like ELSE without IF or nested block without keyword
        // I think that it should be allowed for a step with keyword to have no children
        if (options.includeTotalStepCount()) {
            responseBuilder.totalStepCount(countTotalSteps(rootStep, warnings));
        }

        if (options.includeKeywordStepCount()) {
            responseBuilder.keywordStepCount(countKeywordSteps(rootStep, warnings));
        }

        if (options.includeStepsWithoutActors()) {
            List<String> stepsWithoutActors = findStepsWithoutActors(rootStep, warnings);
            responseBuilder.stepsWithoutActors(stepsWithoutActors);
        }

        if (options.includeNumberedScenario()) {
            List<String> textualScenario = generateTextualScenario(rootStep, options.maxDepth(), warnings);
            responseBuilder.textualScenario(textualScenario);
        }

        if (options.includeLimitedScenario()) {
            Scenario limitedScenario = generateLimitedScenario(scenario, options.maxDepth(), warnings);
            responseBuilder.limitedScenario(limitedScenario);
        }

        // 4. Build and return the final immutable object!
        return responseBuilder.warnings(warnings).build();
    }

    private AnalysisResponse normalize(ScenarioWrapper scenarioWrapper, List<String> warnings) {
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
    private Integer countTotalSteps(Step rootStep, List<String> warnings) {
        if (rootStep == null || rootStep.getSubSteps() == null || rootStep.getSubSteps().isEmpty()) {
            return 0; // Empty scenario -> 0 steps
        }
        Queue<Step> queue = new LinkedList<>();
        queue.addAll(rootStep.getSubSteps());
        int count = 0;
        while(!queue.isEmpty()) {
            Step current = queue.poll();
            count++;
            if (current.getSubSteps() != null) {
                queue.addAll(current.getSubSteps());
            }
        }
        return count;
    }

    /**
     * Counts only steps that begin with a specific keyword (IF, ELSE, FOR EACH).
     */
    private Integer countKeywordSteps(Step rootStep, List<String> warnings) {
        if (rootStep == null || rootStep.getSubSteps() == null || rootStep.getSubSteps().isEmpty()) {
            return 0; // Empty scenario -> 0 steps
        }
        Queue<Step> queue = new LinkedList<>();
        queue.addAll(rootStep.getSubSteps());
        int count = 0;
        while(!queue.isEmpty()) {
            Step current = queue.poll();
            if(current.getKeyword()!=null) count++;
            if (current.getSubSteps() != null) {
                queue.addAll(current.getSubSteps());
            }
        }
        return count;
    }

    /**
     * Returns a list of order numbers (e.g., ["1.2.", "2."]) for steps that lack an actor.
     */
    private List<String> findStepsWithoutActors(Step rootStep, List<String> warnings) {
        List<String> unassignedSteps = new ArrayList<>();
        findStepsWithoutActorsRecursive(rootStep, unassignedSteps);
        return unassignedSteps.isEmpty() ? null : unassignedSteps;
    }

    private void findStepsWithoutActorsRecursive(Step step, List<String> unassignedSteps) {
        for (Step subStep : step.getSubSteps()) {
            if (subStep.getActor() == null) {
                unassignedSteps.add(subStep.getOrderNumber());
            }
            findStepsWithoutActorsRecursive(subStep, unassignedSteps);
        }
    }

    /**
     * Reconstructs the scenario as a list of strings, restricted to a certain depth.
     * If maxDepth is 0, it means "no limit".
     * Format: "1.2. IF: Librarian adds book" with indentation for nesting.
     */
    private List<String> generateTextualScenario(Step rootStep, int maxDepth, List<String> warnings) {
        List<String> scenarioText = new ArrayList<>();
        generateTextualScenarioRecursive(rootStep, scenarioText, 0, maxDepth);
        return scenarioText.isEmpty() ? null : scenarioText;
    }

    private void generateTextualScenarioRecursive(Step step, List<String> scenarioText, int currentDepth, int maxDepth) {
        for (Step subStep : step.getSubSteps()) {
            // Check depth limit
            if (maxDepth > 0 && currentDepth >= maxDepth) {
                break;
            }

            String orderNum = subStep.getOrderNumber();
            String text = buildStepText(subStep);
            String indentation = "  ".repeat(currentDepth);
            scenarioText.add(indentation + orderNum + " " + text);

            // Recurse into children
            generateTextualScenarioRecursive(subStep, scenarioText, currentDepth + 1, maxDepth);
        }
    }

    private String buildStepText(Step step) {
        StringBuilder sb = new StringBuilder();
        
        if (step.getKeyword() != null) {
            sb.append(step.getKeyword()).append(": ");
        }
        
        if (step.getActor() != null) {
            sb.append(step.getActor()).append(" ");
        }
        
        if (step.getCleanText() != null) {
            sb.append(step.getCleanText());
        }
        
        return sb.toString().trim();
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

        return new Scenario(
                scenario.title(),
                scenario.externalActors(),
                scenario.systemActors(),
                copyStepToDepth(scenario.rootStep(), 0, maxDepth)
        );
    }

    private Step copyStepToDepth(Step sourceStep, int currentDepth, int maxDepth) {
        Step copiedStep = new Step();
        copiedStep.setText(sourceStep.getText());
        copiedStep.setKeyword(sourceStep.getKeyword());
        copiedStep.setActor(sourceStep.getActor());
        copiedStep.setCleanText(sourceStep.getCleanText());
        copiedStep.setOrderNumber(sourceStep.getOrderNumber());

        if (maxDepth == 0 || currentDepth < maxDepth) {
            List<Step> copiedSubSteps = new ArrayList<>();
            for (Step subStep : sourceStep.getSubSteps()) {
                copiedSubSteps.add(copyStepToDepth(subStep, currentDepth + 1, maxDepth));
            }
            copiedStep.setSubSteps(copiedSubSteps);
        }

        return copiedStep;
    }
}
