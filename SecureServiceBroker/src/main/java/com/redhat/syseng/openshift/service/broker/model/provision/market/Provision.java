package com.redhat.syseng.openshift.service.broker.model.provision.market;

public class Provision
{
    private String service_id;

    private String organization_guid;

    private String accepts_incomplete;

    private String space_guid;

    private Context context;

    private Parameters parameters;

    private String plan_id;

    public String getService_id ()
    {
        return service_id;
    }

    public void setService_id (String service_id)
    {
        this.service_id = service_id;
    }

    public String getOrganization_guid ()
    {
        return organization_guid;
    }

    public void setOrganization_guid (String organization_guid)
    {
        this.organization_guid = organization_guid;
    }

    public String getAccepts_incomplete ()
    {
        return accepts_incomplete;
    }

    public void setAccepts_incomplete (String accepts_incomplete)
    {
        this.accepts_incomplete = accepts_incomplete;
    }

    public String getSpace_guid ()
    {
        return space_guid;
    }

    public void setSpace_guid (String space_guid)
    {
        this.space_guid = space_guid;
    }

    public Context getContext ()
    {
        return context;
    }

    public void setContext (Context context)
    {
        this.context = context;
    }

    public Parameters getParameters ()
    {
        return parameters;
    }

    public void setParameters (Parameters parameters)
    {
        this.parameters = parameters;
    }

    public String getPlan_id ()
    {
        return plan_id;
    }

    public void setPlan_id (String plan_id)
    {
        this.plan_id = plan_id;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [service_id = "+service_id+", organization_guid = "+organization_guid+", accepts_incomplete = "+accepts_incomplete+", space_guid = "+space_guid+", context = "+context+", parameters = "+parameters+", plan_id = "+plan_id+"]";
    }
}
