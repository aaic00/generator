/*
 *  Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mybatis.generator.api;

import static org.mybatis.generator.internal.util.EqualsUtil.areEqual;
import static org.mybatis.generator.internal.util.HashCodeUtil.SEED;
import static org.mybatis.generator.internal.util.HashCodeUtil.hash;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getCamelCaseString;
import static org.mybatis.generator.internal.util.StringUtility.composeFullyQualifiedTableName;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import org.mybatis.generator.config.Context;

/**
 * @author Jeff Butler
 */
public class FullyQualifiedTable {

    private String introspectedCatalog;

    private String introspectedSchema;

    private String introspectedTableName;

    private String runtimeCatalog;

    private String runtimeSchema;

    private String runtimeTableName;

    private String domainObjectName;

    private String alias;

    private boolean ignoreQualifiersAtRuntime;

    private String beginningDelimiter;

    private String endingDelimiter;

    /**
     * This object is used to hold information related to the table itself, not
     * the columns in the table.
     * 
     * @param introspectedCatalog
     *            the actual catalog of the table as returned from
     *            DatabaseMetaData. This value should only be set if the user
     *            configured a catalog. Otherwise the DatabaseMetaData is
     *            reporting some database default that we don't want in the
     *            generated code.
     * 
     * @param introspectedSchema
     *            the actual schema of the table as returned from
     *            DatabaseMetaData. This value should only be set if the user
     *            configured a schema. Otherwise the DatabaseMetaData is
     *            reporting some database default that we don't want in the
     *            generated code.
     * 
     * @param introspectedTableName
     *            the actual table name as returned from DatabaseMetaData
     * 
     * @param domainObjectName
     *            the configured domain object name for this table. If nothing
     *            is configured, we'll build the domain object named based on
     *            the tableName or runtimeTableName.
     * 
     * @param alias
     *            a configured alias for the table. This alias will be added to
     *            the table name in the SQL
     * 
     * @param ignoreQualifiersAtRuntime
     *            if true, then the catalog and schema qualifiers will be
     *            ignored when composing fully qualified names in the generated
     *            SQL. This is used, for example, when the user needs to specify
     *            a specific schema for generating code but does not want the
     *            schema in the generated SQL
     * 
     * @param runtimeCatalog
     *            this is used to "rename" the catalog in the generated SQL.
     *            This is useful, for example, when generating code against one
     *            catalog that should run with a different catalog.
     * 
     * @param runtimeSchema
     *            this is used to "rename" the schema in the generated SQL. This
     *            is useful, for example, when generating code against one
     *            schema that should run with a different schema.
     * 
     * @param runtimeTableName
     *            this is used to "rename" the table in the generated SQL. This
     *            is useful, for example, when generating code to run with an
     *            Oracle synonym. The user would have to specify the actual
     *            table name and schema for generation, but would want to use
     *            the synonym name in the generated SQL
     * 
     * @param delimitIdentifiers
     *            if true, then the table identifiers will be delimited at
     *            runtime. The delimiter characters are obtained from the
     *            Context.
     */
    public FullyQualifiedTable(String introspectedCatalog,
            String introspectedSchema, String introspectedTableName,
            String domainObjectName, String alias,
            boolean ignoreQualifiersAtRuntime, String runtimeCatalog,
            String runtimeSchema, String runtimeTableName,
            boolean delimitIdentifiers, Context context) {
        super();
        this.introspectedCatalog = introspectedCatalog;
        this.introspectedSchema = introspectedSchema;
        this.introspectedTableName = introspectedTableName;
        this.domainObjectName = domainObjectName;
        this.ignoreQualifiersAtRuntime = ignoreQualifiersAtRuntime;
        this.runtimeCatalog = runtimeCatalog;
        this.runtimeSchema = runtimeSchema;
        this.runtimeTableName = runtimeTableName;

        if (alias == null) {
            this.alias = null;
        } else {
            this.alias = alias.trim();
        }

        beginningDelimiter = delimitIdentifiers ? context
                .getBeginningDelimiter() : ""; //$NON-NLS-1$
        endingDelimiter = delimitIdentifiers ? context.getEndingDelimiter()
                : ""; //$NON-NLS-1$
    }

    public String getIntrospectedCatalog() {
        return introspectedCatalog;
    }

    public String getIntrospectedSchema() {
        return introspectedSchema;
    }

    public String getIntrospectedTableName() {
        return introspectedTableName;
    }

    /**
     * @return
     */
    public String getFullyQualifiedTableNameAtRuntime() {
        StringBuilder localCatalog = new StringBuilder();
        if (!ignoreQualifiersAtRuntime) {
            if (stringHasValue(runtimeCatalog)) {
                localCatalog.append(runtimeCatalog);
            } else if (stringHasValue(introspectedCatalog)) {
                localCatalog.append(introspectedCatalog);
            }
        }
        if (localCatalog.length() > 0) {
            addDelimiters(localCatalog);
        }

        StringBuilder localSchema = new StringBuilder();
        if (!ignoreQualifiersAtRuntime) {
            if (stringHasValue(runtimeSchema)) {
                localSchema.append(runtimeSchema);
            } else if (stringHasValue(introspectedSchema)) {
                localSchema.append(introspectedSchema);
            }
        }
        if (localSchema.length() > 0) {
            addDelimiters(localSchema);
        }

        StringBuilder localTableName = new StringBuilder();
        if (stringHasValue(runtimeTableName)) {
            localTableName.append(runtimeTableName);
        } else {
            localTableName.append(introspectedTableName);
        }
        addDelimiters(localTableName);

        return composeFullyQualifiedTableName(localCatalog
                .toString(), localSchema.toString(), localTableName.toString(),
                '.');
    }

    /**
     * @return
     */
    public String getAliasedFullyQualifiedTableNameAtRuntime() {
        StringBuilder sb = new StringBuilder();

        sb.append(getFullyQualifiedTableNameAtRuntime());

        if (stringHasValue(alias)) {
            sb.append(' ');
            sb.append(alias);
        }

        return sb.toString();
    }

    /**
     * This method returns a string that is the fully qualified table name, with
     * underscores as the separator.
     * 
     * @return the namespace
     */
    public String getIbatis2SqlMapNamespace() {
        String localCatalog = stringHasValue(runtimeCatalog) ? runtimeCatalog
                : introspectedCatalog;
        String localSchema = stringHasValue(runtimeSchema) ? runtimeSchema
                : introspectedSchema;
        String localTable = stringHasValue(runtimeTableName) ? runtimeTableName
                : introspectedTableName;

        return composeFullyQualifiedTableName(
                        ignoreQualifiersAtRuntime ? null : localCatalog,
                        ignoreQualifiersAtRuntime ? null : localSchema,
                        localTable, '_');
    }

    public String getDomainObjectName() {
        if (stringHasValue(domainObjectName)) {
            return domainObjectName;
        } else if (stringHasValue(runtimeTableName)) {
            return getCamelCaseString(runtimeTableName, true);
        } else {
            return getCamelCaseString(introspectedTableName, true);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FullyQualifiedTable)) {
            return false;
        }

        FullyQualifiedTable other = (FullyQualifiedTable) obj;

        return areEqual(this.introspectedTableName,
                other.introspectedTableName)
                && areEqual(this.introspectedCatalog,
                        other.introspectedCatalog)
                && areEqual(this.introspectedSchema,
                        other.introspectedSchema);
    }

    @Override
    public int hashCode() {
        int result = SEED;
        result = hash(result, introspectedTableName);
        result = hash(result, introspectedCatalog);
        result = hash(result, introspectedSchema);

        return result;
    }

    @Override
    public String toString() {
        return composeFullyQualifiedTableName(
                introspectedCatalog, introspectedSchema, introspectedTableName,
                '.');
    }

    public String getAlias() {
        return alias;
    }

    /**
     * Calculates a Java package fragment based on the table catalog and schema.
     * If qualifiers are ignored, then this method will return an empty string
     * 
     * @return the subpackage for this table
     */
    public String getSubPackage() {
        StringBuilder sb = new StringBuilder();
        if (!ignoreQualifiersAtRuntime) {
            if (stringHasValue(runtimeCatalog)) {
                sb.append('.');
                sb.append(runtimeCatalog.toLowerCase());
            } else if (stringHasValue(introspectedCatalog)) {
                sb.append('.');
                sb.append(introspectedCatalog.toLowerCase());
            }

            if (stringHasValue(runtimeSchema)) {
                sb.append('.');
                sb.append(runtimeSchema.toLowerCase());
            } else if (stringHasValue(introspectedSchema)) {
                sb.append('.');
                sb.append(introspectedSchema.toLowerCase());
            }
        }

        // TODO - strip characters that are not valid in package names
        return sb.toString();
    }

    private void addDelimiters(StringBuilder sb) {
        if (stringHasValue(beginningDelimiter)) {
            sb.insert(0, beginningDelimiter);
        }

        if (stringHasValue(endingDelimiter)) {
            sb.append(endingDelimiter);
        }
    }
}