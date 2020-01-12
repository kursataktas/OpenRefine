/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package org.openrefine.model.changes;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;
import java.util.Properties;

import org.openrefine.ProjectManager;
import org.openrefine.history.Change;
import org.openrefine.model.Cell;
import org.openrefine.model.ColumnMetadata;
import org.openrefine.model.Project;

public class CellChange implements Change {

    final public int row;
    final public int cellIndex;
    final public Cell oldCell;
    final public Cell newCell;

    public CellChange(int row, int cellIndex, Cell oldCell, Cell newCell) {
        this.row = row;
        this.cellIndex = cellIndex;
        this.oldCell = oldCell;
        this.newCell = newCell;
    }

    @Override
    public void apply(Project project) {
        project.rows.get(row).setCell(cellIndex, newCell);

        ColumnMetadata column = project.columnModel.getColumnByCellIndex(cellIndex);
        ProjectManager.singleton.getInterProjectModel().flushJoinsInvolvingProjectColumn(project.id, column.getName());
    }

    @Override
    public void revert(Project project) {
        project.rows.get(row).setCell(cellIndex, oldCell);

        ColumnMetadata column = project.columnModel.getColumnByCellIndex(cellIndex);
        ProjectManager.singleton.getInterProjectModel().flushJoinsInvolvingProjectColumn(project.id, column.getName());
    }

    @Override
    public void save(Writer writer, Properties options) throws IOException {
        writer.write("row=");
        writer.write(Integer.toString(row));
        writer.write('\n');
        writer.write("cell=");
        writer.write(Integer.toString(cellIndex));
        writer.write('\n');

        writer.write("old=");
        if (oldCell != null) {
            oldCell.save(writer); // one liner
        }
        writer.write('\n');

        writer.write("new=");
        if (newCell != null) {
            newCell.save(writer); // one liner
        }
        writer.write('\n');

        writer.write("/ec/\n"); // end of change marker
    }

    static public CellChange load(LineNumberReader reader) throws Exception {
        int row = -1;
        int cellIndex = -1;
        Cell oldCell = null;
        Cell newCell = null;

        String line;
        while ((line = reader.readLine()) != null && !"/ec/".equals(line)) {
            int equal = line.indexOf('=');
            CharSequence field = line.subSequence(0, equal);
            String value = line.substring(equal + 1);

            if ("row".equals(field)) {
                row = Integer.parseInt(value);
            } else if ("cell".equals(field)) {
                cellIndex = Integer.parseInt(value);
            } else if ("new".equals(field) && value.length() > 0) {
                newCell = Cell.loadStreaming(value);
            } else if ("old".equals(field) && value.length() > 0) {
                oldCell = Cell.loadStreaming(value);
            }
        }

        return new CellChange(row, cellIndex, oldCell, newCell);
    }
}
