/**
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */
package com.demandware.carbonj.service.db.points;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StagingFileSetCollector
{
    private static final Logger log = LoggerFactory.getLogger( StagingFileSetCollector.class );
    private File dir;

    public StagingFileSetCollector(File dir)
    {
        this.dir = Preconditions.checkNotNull( dir );
    }

    synchronized public List<SortedStagingFile> collectEligibleFiles(Map<StagingFileSet, StagingFile> files)
    {
        List<SortedStagingFile> sortedFiles = new ArrayList<>(  );
        List<StagingFileSet> names = new ArrayList<>( files.keySet() );
        names.sort( (o1, o2) -> o1.from - o2.from );

        names.stream()
                .filter( fs -> fs.needsCollection( files.get( fs ).lastModified() ) )
                .forEach( fs ->
                                {
                                    log.debug( "processing staging file: [" + fs + "]" );
                                    StagingFile f = files.remove( fs );
                                    f.close();
                                    Optional<String> lastSorted = fs.getLastSortedFileName( dir );
                                    log.debug( "sorting ..." );
                                    SortedStagingFile sortedFile = f.sort(lastSorted);
                                    log.debug("sorted file: [" + sortedFile + "]");
                                    sortedFiles.add( sortedFile );
                                }
                );
        log.debug("sorted files: [" + sortedFiles + "]");
        return sortedFiles;
    }
}
