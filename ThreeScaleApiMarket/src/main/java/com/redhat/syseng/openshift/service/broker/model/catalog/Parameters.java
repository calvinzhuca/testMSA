/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

                 
public class Parameters
{
    private String $schema;

    private String additionalProperties;

    private Properties properties;

    private String type;

    private String[] required;

    public String get$schema ()
    {
        return $schema;
    }

    public void set$schema (String $schema)
    {
        this.$schema = $schema;
    }

    public String getAdditionalProperties ()
    {
        return additionalProperties;
    }

    public void setAdditionalProperties (String additionalProperties)
    {
        this.additionalProperties = additionalProperties;
    }

    public Properties getProperties ()
    {
        return properties;
    }

    public void setProperties (Properties properties)
    {
        this.properties = properties;
    }

    public String getType ()
    {
        return type;
    }

    public void setType (String type)
    {
        this.type = type;
    }

    public String[] getRequired ()
    {
        return required;
    }

    public void setRequired (String[] required)
    {
        this.required = required;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [$schema = "+$schema+", additionalProperties = "+additionalProperties+", properties = "+properties+", type = "+type+", required = "+required+"]";
    }
}