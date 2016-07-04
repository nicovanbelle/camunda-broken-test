package be.camunda.bpm.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Named;

@LocalBean
@Stateless
@Named("notificationApproved")
public class NotificationApproved implements ExecutionListener {


    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {

    }
}
