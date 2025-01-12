/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.kylin.cube.model;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.kylin.dimension.DateDimEnc;
import org.apache.kylin.dimension.DictionaryDimEnc;
import org.apache.kylin.dimension.DimensionEncoding;
import org.apache.kylin.dimension.DimensionEncodingFactory;
import org.apache.kylin.dimension.TimeDimEnc;
import org.apache.kylin.metadata.datatype.DataType;
import org.apache.kylin.metadata.model.TblColRef;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * @author yangli9
 * 
 */
@JsonAutoDetect(fieldVisibility = Visibility.NONE, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class RowKeyColDesc {

    @JsonProperty("column")
    private String column;
    @JsonProperty("encoding")
    private String encoding;
    @JsonProperty("isShardBy")
    private boolean isShardBy;//usually it is ultra high cardinality column, shard by such column can reduce the agg cache for each shard
    @JsonProperty("index")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String index;

    // computed
    private String encodingName;
    private String[] encodingArgs;
    private int bitIndex;
    private TblColRef colRef;

    public void init(int index, Map<String, TblColRef> colNameAbbr, CubeDesc cubeDesc) {
        column = column.toUpperCase();
        bitIndex = index;
        colRef = colNameAbbr.get(column);
        if (colRef == null) {
            throw new IllegalArgumentException("Cannot find rowkey column " + column + " in cube " + cubeDesc);
        }

        Preconditions.checkState(StringUtils.isNotEmpty(this.encoding));
        Object[] encodingConf = DimensionEncoding.parseEncodingConf(this.encoding);
        encodingName = (String) encodingConf[0];
        encodingArgs = (String[]) encodingConf[1];

        if (!DimensionEncodingFactory.isVaildEncoding(this.encodingName))
            throw new IllegalArgumentException("Not supported row key col encoding: '" + this.encoding + "'");

        // convert date/time dictionary to DimensionEncoding implicitly, date/time dictionary is deprecated
        if (DictionaryDimEnc.ENCODING_NAME.equals(encodingName)) {
            DataType type = colRef.getType();
            if (type.isDate()) {
                encoding = encodingName = DateDimEnc.ENCODING_NAME;
            }
            if (type.isTime() || type.isTimestamp() || type.isDatetime()) {
                encoding = encodingName = TimeDimEnc.ENCODING_NAME;
            }
        }
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public boolean isShardBy() {
        return isShardBy;
    }

    public void setShardBy(boolean shardBy) {
        isShardBy = shardBy;
    }

    public String getEncodingName() {
        return encodingName;
    }

    public String[] getEncodingArgs() {
        return encodingArgs;
    }

    public boolean isUsingDictionary() {
        return DictionaryDimEnc.ENCODING_NAME.equals(encodingName);
    }

    public int getBitIndex() {
        return bitIndex;
    }

    public TblColRef getColRef() {
        return colRef;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("column", column).add("encoding", encoding).toString();
    }

}