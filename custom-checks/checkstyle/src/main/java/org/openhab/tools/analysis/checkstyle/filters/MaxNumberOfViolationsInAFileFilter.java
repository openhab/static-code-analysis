/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.filters;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Filter;

/**
 * @author Lyubomir Papazov - Initial contribution
 *
 *         This filter sets the maximum number of violations to be reported for
 *         a single file for certain checks, specified in the setChecks method.
 *
 *         In order to use this module, the user must create a
 *         module with the same name as the fully qualified class name of the filter within the checker module.
 *         The module should have properties checks and maxNumberOfViolationsReported.
 */
public class MaxNumberOfViolationsInAFileFilter extends AutomaticBean implements Filter {

    // The default value if the maxNumberOfViolationsToBeReported property isn't set
    private int maxNumberOfViolationsToBeReported = Integer.MAX_VALUE;
    private List<String> checkNames;
    private CurrentlyCheckedFile currentlyCheckedFile;

    private class CurrentlyCheckedFile {
        private Map<String, Integer> checkNamesToViolationCount;
        String fileName;

        public CurrentlyCheckedFile(String fileName) {
            this.fileName = fileName;
            initializeCheckNamesToViolationCount();
        }

        private void initializeCheckNamesToViolationCount() {
            checkNamesToViolationCount = checkNames.stream().collect(Collectors.toMap(x -> x, x -> 0));
        }

        /**
         * @return the name of the file currently being checked.
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Should be called whenever a violation is found in the file being checked.
         * 
         * @param checkFullName -the fully qualified class name of the check
         */
        public void addViolationOfCheck(String checkFullName) {
            checkNamesToViolationCount.put(checkFullName, checkNamesToViolationCount.get(checkFullName) + 1);
        }

        /**
         * Returns the number of times a check violation has been reported for the current file
         * 
         * @param checkFullName - the fully qualified class name of the check 
         * @return - the number of times this check has been violated in the current file.
         */
        public Integer numberOfViolationsOfCheck(String checkFullName) {
            return checkNamesToViolationCount.get(checkFullName);
        }
    }

    @Override
    // Every violation for every check in each file triggers AuditEvent
    // The total number of different AuditEvents is:
    // num_checks*num_files*num_violations_in_curr_file_for_curr_check
    public boolean accept(AuditEvent event) {

        // The fully qualified class name of the check
        final String checkFullName = event.getSourceName();

        // If the current check is not in the list of checks that have a limit how many times to be logged,
        // this auditEvent will be accepted.
        if (!checkNames.contains(checkFullName)) {
            return true;
        }

        // All checkstyle checks are executed file by file. If the fileName of the
        // current event is different than the currentlyCheckedFile's name, it means that checkstyle
        // starts checking a new file.
        if (currentlyCheckedFile == null || currentlyCheckedFile.getFileName() != event.getFileName()) {
            currentlyCheckedFile = new CurrentlyCheckedFile(event.getFileName());
        }

        // Update the number of reported violations for this check in the currently checked file 
        currentlyCheckedFile.addViolationOfCheck(checkFullName);

        // Unless the number of violations of this check for this file exceeds the maxNumberOfViolations allowed to be reported,
        // then this auditEvent will be logged
        return currentlyCheckedFile.numberOfViolationsOfCheck(checkFullName) <= maxNumberOfViolationsToBeReported;
    }

    /**
     * @param checkNames - The fully qualified java names of the checks as strings. Could
     *            be set as a module property in the xml configuration.
     */
    public void setChecks(String[] checkNames) {
        this.checkNames = Arrays.asList(checkNames);
    }

    /**
     * @param count - The max number of violations to be reported for one class.
     */
    public void setMaxNumberOfViolationsReported(String count) {
        this.maxNumberOfViolationsToBeReported = Integer.parseInt(count);
    }

    @Override
    protected void finishLocalSetup() throws CheckstyleException {
        // No code by default
    }
}
