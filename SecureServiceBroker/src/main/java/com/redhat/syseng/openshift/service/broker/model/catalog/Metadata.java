/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.syseng.openshift.service.broker.model.catalog;

public class Metadata
{
    private String documentationUrl;

    private String longDescription;

    private String displayName;

    public String getDocumentationUrl ()
    {
        return documentationUrl;
    }

    public void setDocumentationUrl (String documentationUrl)
    {
        this.documentationUrl = documentationUrl;
    }

    public String getLongDescription ()
    {
        return longDescription;
    }

    public void setLongDescription (String longDescription)
    {
        this.longDescription = longDescription;
    }

    public String getDisplayName ()
    {
        return displayName;
    }

    public void setDisplayName (String displayName)
    {
        this.displayName = displayName;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [documentationUrl = "+documentationUrl+", longDescription = "+longDescription+", displayName = "+displayName+"]";
    }
}
