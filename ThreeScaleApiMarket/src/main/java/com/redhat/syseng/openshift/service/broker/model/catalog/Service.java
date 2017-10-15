/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

/**
 *
 * @author czhu
 */
public class Service
{
    private String id;

    private Plan[] plans;

    private String description;

    private String name;

    private Metadata metadata;

    private boolean bindable;

    public String getId ()
    {
        return id;
    }

    public void setId (String id)
    {
        this.id = id;
    }

    public Plan[] getPlans ()
    {
        return plans;
    }

    public void setPlans (Plan[] plans)
    {
        this.plans = plans;
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

    public Metadata getMetadata ()
    {
        return metadata;
    }

    public void setMetadata (Metadata metadata)
    {
        this.metadata = metadata;
    }

    public boolean getBindable ()
    {
        return bindable;
    }

    public void setBindable (boolean bindable)
    {
        this.bindable = bindable;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [id = "+id+", plans = "+plans+", description = "+description+", name = "+name+", metadata = "+metadata+", bindable = "+bindable+"]";
    }
}