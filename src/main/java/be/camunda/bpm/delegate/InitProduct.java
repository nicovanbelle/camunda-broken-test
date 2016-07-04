package be.camunda.bpm.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.ejb.Stateless;
import javax.inject.Named;

@Stateless
@Named
public class InitProduct implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
    }

}
