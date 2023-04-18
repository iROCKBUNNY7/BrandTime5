package com.jfirer.jfire.core.beandescriptor;

public class SelectedBeanFactoryInstanceDescriptor implements InstanceDescriptor
{
    private String   selectedBeanFactoryBeanName;
    private Class<?> selectedBeanFactoryBeanClass;
    private Class    ckass;

    public SelectedBeanFactoryInstanceDescriptor(Class<?> selectedBeanFactoryBeanClass, Class ckass)
    {
        this.selectedBeanFactoryBeanClass = selectedBeanFactoryBeanClass;
        this.ckass = ckass;
    }

    public SelectedBeanFactoryInstanceDescriptor(String selectedBeanFactoryBeanName, Class ckass)
    {
        this.selectedBeanFactoryBeanName = selectedBeanFactoryBeanName;
        this.ckass = ckass;
    }

    @Override
    public Object newInstanceDescriptor()
    {
        return ckass;
    }

    @Override
    public String selectedBeanFactoryBeanName()
    {
        return selectedBeanFactoryBeanName;
    }

    @Override
    public Class<?> selectedBeanFactoryBeanClass()
    {
        return selectedBeanFactoryBeanClass;
    }
}
