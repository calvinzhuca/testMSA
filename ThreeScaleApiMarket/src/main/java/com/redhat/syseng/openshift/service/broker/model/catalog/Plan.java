/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

public class Plan
{
    private String id;

    private Schemas schemas;

    private String free;

    private String description;

    private String name;

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public Schemas getSchemas ()
    {
        return schemas;
    }

    public void setSchemas (Schemas schemas)
    {
        this.schemas = schemas;
    }

    public String getFree ()
    {
        return free;
    }

    public void setFree (String free)
    {
        this.free = free;
    }

    public String getDescription ()
    {
        return description;
    }

    public void setDescription (String description)
    {
        this.description = description;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", schemas = "+schemas+", free = "+free+", description = "+description+", name = "+name+"]";
    }
}
