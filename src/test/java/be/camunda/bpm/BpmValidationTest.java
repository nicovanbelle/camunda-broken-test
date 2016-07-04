package be.camunda.bpm;

import be.camunda.bpm.delegate.InitProduct;
import be.camunda.bpm.delegate.NotificationApproved;
import be.camunda.bpm.domain.ProductDto;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.assertions.ProcessEngineTests;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;

/**
 * https://github.com/camunda/camunda-bpm-assert
 */
public class BpmValidationTest {

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    private DateTime startTime;

    @Mock
    private NotificationApproved notificationApproved;

    @Mock
    private InitProduct initProduct;

    @Before
    public void setUp() {
        startTime = getStartDateTime();
        processEngineRule.setCurrentTime(startTime.toDate());

        MockitoAnnotations.initMocks(this);

        Mocks.register("notificationApproved", notificationApproved);
        Mocks.register("initProduct", initProduct);
    }

    @After
    public void tearDown() {
        Mocks.reset();
    }

    @Test
    @Deployment(resources = {"bpmn/product-order-process.bpmn"})
    public void testProductOrderProcess() throws InterruptedException {
        final String groupA = "groupA";
        final String groupB = "groupB";
        final String groupC = "groupC";
        final String groupAPerson = "groupAPerson";
        final String groupBPerson = "groupBPerson";
        final String groupCPerson = "groupCPerson";

        VariableMap startVarMap = Variables.createVariables()
                .putValue("initiator", groupCPerson)
                .putValue("initiatorGroup", groupC)
                .putValue("startDate", Variables.objectValue(getStartDateTime())
                        .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
                        .create());

        ProcessInstance processInstance = ProcessEngineTests.runtimeService()
                .startProcessInstanceByKey("productOrderProcess", startVarMap);

        // Set processInstance in AbstractProcessAssert scope
        assertThat(processInstance).isNotNull();

        // Execute asynchronous before job of first task
        execute(job());

        assertThat(processInstance)
                .isStarted()
                .isNotEnded()
                .task()
                .hasDefinitionKey("Create")
                .isAssignedTo(groupCPerson);

        complete(task(), Variables.createVariables().putValue("confirmationRequired", Boolean.FALSE)
                                                    .putValue("groupA", groupA)
                                                    .putValue("groupB", groupB));

        /*
         * Check if Confirm task is skipped (no confirmation required)
         * and candidateGroup is our installer group
         */
        assertThat(processInstance)
                .task()
                .hasDefinitionKey("Approve")
                .isAssignedTo(groupCPerson)
                .hasDueDate(startTime.plusDays(14).toDate());

        List<ProductDto> products = new ArrayList<>();
        products.add(new ProductDto(true));

        complete(claim(task(), groupCPerson),
                Variables.createVariables()
                .putValue("woProducts", products));

        execute(job());

        // Assert subProcesses

        assertThat(processInstance)
                .task("PlanExecution")
                .hasCandidateGroup(groupA);

        complete(claim(task("PlanExecution"), groupAPerson));

        assertThat(processInstance)
                .task("PrepareParts")
                .hasCandidateGroup(groupB);

        complete(claim(task("PrepareParts"), groupBPerson));

        assertThat(processInstance).isEnded();
    }

    private DateTime getStartDateTime() {
        return new DateTime()
                .withHourOfDay(23)
                .withMinuteOfHour(59)
                .withSecondOfMinute(59)
                .withMillisOfSecond(0);
    }

}
