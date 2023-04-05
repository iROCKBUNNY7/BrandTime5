package com.jfirer.jfire.core.prepare.annotation.condition;

import java.util.LinkedList;
import java.util.List;

public class ErrorMessage
{
    private List<String> list = new LinkedList<String>();

    public void addErrorMessage(String errorMessage)
    {
        list.add(errorMessage);
    }

    public List<String> getList()
    {
        return list;
    }

    public void clear()
    {
        list.clear();
    }
}
