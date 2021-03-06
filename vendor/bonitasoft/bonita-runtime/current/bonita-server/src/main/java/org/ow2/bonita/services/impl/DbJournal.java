/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 * 
 * Modified by Matthieu Chaffotte, Elias Ricken de Medeiros - BonitaSoft S.A.
 **/
package org.ow2.bonita.services.impl;

import java.util.Date;
import java.util.Set;

import org.ow2.bonita.facade.def.InternalProcessDefinition;
import org.ow2.bonita.facade.def.element.MetaData;
import org.ow2.bonita.facade.def.element.impl.MetaDataImpl;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition.ProcessState;
import org.ow2.bonita.facade.runtime.ActivityInstance;
import org.ow2.bonita.facade.runtime.ActivityState;
import org.ow2.bonita.facade.runtime.Category;
import org.ow2.bonita.facade.runtime.InstanceState;
import org.ow2.bonita.facade.runtime.StateUpdate;
import org.ow2.bonita.facade.runtime.TaskInstance;
import org.ow2.bonita.facade.runtime.VariableUpdate;
import org.ow2.bonita.facade.runtime.impl.InternalActivityInstance;
import org.ow2.bonita.facade.runtime.impl.InternalProcessInstance;
import org.ow2.bonita.facade.runtime.impl.InternalVariableUpdate;
import org.ow2.bonita.facade.uuid.ActivityInstanceUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.persistence.JournalDbSession;
import org.ow2.bonita.runtime.VariablesOptions;
import org.ow2.bonita.services.Journal;
import org.ow2.bonita.type.Variable;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.EnvTool;
import org.ow2.bonita.util.ExceptionManager;
import org.ow2.bonita.util.Misc;
import org.ow2.bonita.util.VariableUtil;

/**
 * @author Guillaume Porcher
 *
 */
public class DbJournal extends AbstractDbQuerier implements Journal {

  public DbJournal(final String persistenceServiceName) {
    super(persistenceServiceName);
  }

  public JournalDbSession getDbSession() {
    return EnvTool.getJournalDbSession(getPersistenceServiceName());
  }

  public void remove(final InternalProcessDefinition pack) {
    Misc.checkArgsNotNull(pack);
    getDbSession().delete(pack);
  }
  
  public void removeExecution(long id) {
    Misc.checkArgsNotNull(id);
	getDbSession().deleteExecution(id);
  }

  public void remove(final InternalProcessInstance processInst) {
    Misc.checkArgsNotNull(processInst);
    processInst.removeAttachments();
    getDbSession().delete(processInst);
//TODO
  }
  
  public void recordEnterActivity(final ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);
    
    final InternalProcessInstance processInstance = getProcessInstance(activityInstance.getProcessInstanceUUID());
    processInstance.addActivity(activityInstance);
  }

  public void recordBodyStarted(final ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);
    if (!activityInstance.isTask()) {
      ((InternalActivityInstance)activityInstance).setActivityState(ActivityState.EXECUTING, BonitaConstants.SYSTEM_USER);
    }
  }

  public void recordBodyEnded(final ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);
    if (!activityInstance.isTask()) {
    	((InternalActivityInstance) activityInstance).setActivityState(ActivityState.FINISHED, BonitaConstants.SYSTEM_USER);
    }
  }

  public void recordBodyAborted(final ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);
    ((InternalActivityInstance) activityInstance).setActivityState(ActivityState.ABORTED, BonitaConstants.SYSTEM_USER);
  }

  public void recordBodyCancelled(final ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);
    ((InternalActivityInstance) activityInstance).setActivityState(ActivityState.CANCELLED, EnvTool.getUserId());
  }
  
  public void recordActivityFailed(ActivityInstance activityInstance) {
    Misc.checkArgsNotNull(activityInstance);
    ((InternalActivityInstance) activityInstance).setActivityState(ActivityState.FAILED, BonitaConstants.SYSTEM_USER);
  }
  
  public void recordActivitySkipped(ActivityInstance activityInstance, String loggedInUserId) {
    Misc.checkArgsNotNull(activityInstance, loggedInUserId);
    ((InternalActivityInstance) activityInstance).setActivityState(ActivityState.SKIPPED, loggedInUserId);
  }

  public void recordInstanceEnded(final ProcessInstanceUUID instanceUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(instanceUUID);

    final InternalProcessInstance instanceRecord = getProcessInstance(instanceUUID);

    String message = ExceptionManager.getInstance().getFullMessage(
  			"bsi_DJ_1", instanceUUID);
    Misc.badStateIfNull(instanceRecord, message);
    instanceRecord.setInstanceState(InstanceState.FINISHED, loggedInUserId);

  }

  public void recordInstanceAborted(final ProcessInstanceUUID instanceUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(instanceUUID);

    final InternalProcessInstance instanceRecord = getProcessInstance(instanceUUID);

    String message = ExceptionManager.getInstance().getFullMessage(
  			"bsi_DJ_2", instanceUUID);
    Misc.badStateIfNull(instanceRecord, message);
    instanceRecord.setInstanceState(InstanceState.ABORTED, loggedInUserId);

  }

  public void recordInstanceCancelled(final ProcessInstanceUUID instanceUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(instanceUUID);

    final InternalProcessInstance instanceRecord = getProcessInstance(instanceUUID);

    String message = ExceptionManager.getInstance().getFullMessage(
  			"bsi_DJ_3", instanceUUID);
    Misc.badStateIfNull(instanceRecord, message);
    instanceRecord.setInstanceState(InstanceState.CANCELLED, loggedInUserId);
  }

  public void recordInstanceStarted(final InternalProcessInstance instance, final String loggedInUserId) {
    Misc.checkArgsNotNull(instance);

    if (instance.getParentInstanceUUID() != null) {
      InternalProcessInstance parentInstance = getProcessInstance(instance.getParentInstanceUUID());
    	parentInstance.addChildInstance(instance.getUUID());
    } 
    
    String message = ExceptionManager.getInstance().getFullMessage("bsi_DJ_4");
    Misc.badStateIfNull(loggedInUserId, message);
    instance.setInstanceState(InstanceState.STARTED, loggedInUserId);
    getDbSession().save(instance);
  }

  public void recordTaskFinished(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance activity = getTaskInstance(taskUUID);
    String message = ExceptionManager.getInstance().getFullMessage(
  			"bsi_DJ_5", taskUUID);
    Misc.badStateIfNull(activity, message);
    ((InternalActivityInstance)activity).setActivityState(ActivityState.FINISHED, loggedInUserId);
  }

  public void recordTaskReady(final ActivityInstanceUUID taskUUID, final Set<String> candidates, final String userId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance activity = getTaskInstance(taskUUID);
    String message = ExceptionManager.getInstance().getFullMessage(
  			"bsi_DJ_6", taskUUID);
    Misc.badStateIfNull(activity, message);
    if (userId != null) {
      ((InternalActivityInstance)activity).setTaskAssign(ActivityState.READY, BonitaConstants.SYSTEM_USER, userId);  
    } else {
      ((InternalActivityInstance)activity).setTaskAssign(ActivityState.READY, BonitaConstants.SYSTEM_USER, candidates);
    }
    
  }

  public void recordTaskStarted(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance activity = getTaskInstance(taskUUID);
    String message = ExceptionManager.getInstance().getFullMessage(
  			"bsi_DJ_7", taskUUID);
    Misc.badStateIfNull(activity, message);
    ((InternalActivityInstance)activity).setActivityState(ActivityState.EXECUTING, loggedInUserId);
  }

  public void recordTaskResumed(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance task = getTaskInstance(taskUUID);
    ActivityState stateBeforeSuspend = null;
	for (StateUpdate su : task.getStateUpdates()) {
		if (su.getActivityState().equals(ActivityState.SUSPENDED)) {
			stateBeforeSuspend = su.getInitialState();
		}
	}
	
    ((InternalActivityInstance)task).setActivityState(stateBeforeSuspend, loggedInUserId);
  }
  public void recordTaskSuspended(final ActivityInstanceUUID taskUUID, final String loggedInUserId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance activity = getTaskInstance(taskUUID);
    ((InternalActivityInstance)activity).setActivityState(ActivityState.SUSPENDED, loggedInUserId);
  }
	
  public void recordTaskSkipped(ActivityInstanceUUID taskUUID,
			String loggedInUserId) {
  	Misc.checkArgsNotNull(taskUUID);
    final TaskInstance activity = getTaskInstance(taskUUID);
    ((InternalActivityInstance)activity).setActivityState(ActivityState.SKIPPED, loggedInUserId);

	}

  public void recordTaskAssigned(final ActivityInstanceUUID taskUUID, 
		  final ActivityState taskState, final String loggedInUserId,
		  final Set<String> candidates, final String assignedUserId) {
    Misc.checkArgsNotNull(taskUUID);

    final TaskInstance activity = getTaskInstance(taskUUID);
    if (assignedUserId != null) {
      ((InternalActivityInstance)activity).setTaskAssign(taskState, loggedInUserId, assignedUserId);
    } else {
      ((InternalActivityInstance)activity).setTaskAssign(taskState, loggedInUserId, candidates);
    }
  }

  public void recordProcessDeployed(final InternalProcessDefinition processDef, final String userId) {
    Misc.checkArgsNotNull(processDef);
    processDef.setState(ProcessState.ENABLED);
    processDef.setDeployedDate(new Date());
    processDef.setDeployedBy(userId);
    getDbSession().save(processDef);
  }
  
  public void recordProcessEnable(final InternalProcessDefinition processDef) {
    Misc.checkArgsNotNull(processDef);
    processDef.setState(ProcessState.ENABLED);
  }
  
  public void recordProcessDisable(final InternalProcessDefinition processDef) {
    Misc.checkArgsNotNull(processDef);
    processDef.setState(ProcessState.DISABLED);
  }

  public void recordProcessArchive(final InternalProcessDefinition processDef, final String userId) {
    Misc.checkArgsNotNull(processDef);
    processDef.setUndeployedBy(userId);
    processDef.setUndeployedDate(new Date());
    processDef.setState(ProcessState.ARCHIVED);
  }

  public void recordProcessUndeployed(final InternalProcessDefinition processDef) {
    recordProcessDisable(processDef);
  }

  public void recordActivityVariableUpdated(final String variableId, final Object variableValue,
      final ActivityInstanceUUID activityUUID, final String userId) {
    Misc.checkArgsNotNull(variableId, activityUUID, userId);

    final ActivityInstance activityInst = getActivityInstance(activityUUID);
    final Variable v = VariableUtil.createVariable(activityInst.getProcessDefinitionUUID(), variableId, variableValue);
    
    final VariablesOptions variablesOptions = EnvTool.getVariablesOptions();
    if (variablesOptions.isStoreHistory()) {
      final VariableUpdate varUpdate = new InternalVariableUpdate(new Date(), userId, variableId, v);
      ((InternalActivityInstance) activityInst).addVariableUpdate(varUpdate);
      ((InternalActivityInstance) activityInst).updateLastUpdateDate();
    } else {
      ((InternalActivityInstance) activityInst).setVariableValue(variableId, v);
    }
  }

  public void recordInstanceVariableUpdated(final String variableId, final Object variableValue,
      final ProcessInstanceUUID instanceUUID, final String userId) {
    Misc.checkArgsNotNull(variableId, instanceUUID, userId);

    final InternalProcessInstance processInst = getProcessInstance(instanceUUID);
    final Variable v = VariableUtil.createVariable(processInst.getProcessDefinitionUUID(), variableId, variableValue);

    final VariablesOptions variablesOptions = EnvTool.getVariablesOptions();
    if (variablesOptions.isStoreHistory()) {
      final VariableUpdate varUpdate = new InternalVariableUpdate(new Date(), userId, variableId, v);
      processInst.addVariableUpdate(varUpdate);
      processInst.updateLastUpdateDate();
    } else {
      processInst.setVariableValue(variableId, v);
    }
  }

  public void recordActivityPriorityUpdated(ActivityInstanceUUID activityUUID, int priority) {
    Misc.checkArgsNotNull(activityUUID);
    ActivityInstance activityInstance = getActivityInstance(activityUUID);
    ((InternalActivityInstance)activityInstance).setPriority(priority);
  }

  public void storeMetaData(String key, String value) {
    Misc.checkArgsNotNull(key, value);
    MetaDataImpl data = getInternalMetaData(key);
    if (data == null) {
      data = new MetaDataImpl(key, value);
      getDbSession().save(data);
    } else {
      data.setValue(value);
    }
  }

  private MetaDataImpl getInternalMetaData(String key) {
    Misc.checkArgsNotNull(key);
    return getDbSession().getMetaData(key);
  }

  public String getMetaData(String key) {    
    final MetaData data = getInternalMetaData(key);
    if (data == null) {
      return null;
    }
    return data.getValue();
  }

  public void deleteMetaData(String key) {
    Misc.checkArgsNotNull(key);
    MetaDataImpl data = getInternalMetaData(key);
    if (data != null) {
      getDbSession().delete(data);
    }
  }

  public long getLockedMetadata(String key) {
    Misc.checkArgsNotNull(key);
    return getDbSession().getLockedMetadata(key);
  }
  
  public void removeLockedMetadata(String key) {
    Misc.checkArgsNotNull(key);
    getDbSession().lockMetadata(key);
  }
  
  public void lockMetadata(String key) {
    Misc.checkArgsNotNull(key);
    getDbSession().lockMetadata(key);
  }
  
  public void updateLockedMetadata(String key, long value) {
    Misc.checkArgsNotNull(key);
    getDbSession().updateLockedMetadata(key, value);
  }

  public void recordNewCategory(Category category) {
    Misc.checkArgsNotNull(category);
    getDbSession().save(category);
  }

}
