/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.tools.analysis.checkstyle.filters;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.filters.SuppressionFilter;

/**
 * @author Lyubomir Papazov - Initial contribution
 * 
 *         A filter that suppresses all checks for files that are ignored by
 *         git. In order to activate the filter, A module with the full class
 *         name should be added within the Checker module in the rules file.
 */
public class GitIgnoredFilesFilter extends SuppressionFilter {

    private Set<Path> ignoredFiles = null;

    private final Log logger = LogFactory.getLog(GitIgnoredFilesFilter.class);

    public GitIgnoredFilesFilter() throws NoWorkTreeException, GitAPIException {
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository;
        try {
            repository = repositoryBuilder.findGitDir().setMustExist(true).build();
            Path pathToRepo = repository.getWorkTree().toPath();
            Git git = new Git(repository);
            Set<String> ignoredFileNames = git.status().call().getIgnoredNotInIndex();
            ignoredFiles = ignoredFileNames.stream().map(pathToRepo::resolve).collect(Collectors.toSet());
        } catch (IOException e) {
            logger.error("An error occurred trying to get all .git ignored files.", e);
        }
    }

    /*
     * Determines whether or not a filtered AuditEvent is accepted.
     *  If the file is ignored by git, the AuditEvent is not accepted.
     */
    @Override
    public boolean accept(AuditEvent event) {
        if (ignoredFiles == null || ignoredFiles.isEmpty()) {
            return true;
        }
        String fileName = event.getFileName();
        return !ignoredFiles.contains(Paths.get(fileName));
    }
}
