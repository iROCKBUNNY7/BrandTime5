package com.jfireframework.context.test.function.event;

import javax.annotation.Resource;
import com.jfireframework.coordinator.api.CoordinatorHandler;
import com.jfireframework.coordinator.bus.CoordinatorBuses;
import com.jfireframework.coordinator.util.RunnerMode;
import com.jfireframework.jfire.JfireInitFinish;
import com.jfireframework.jfire.coordinator.CoordinatorRegisterHelper;

@Resource
public class HaftHandler implements CoordinatorHandler<UserPhone>, JfireInitFinish
{
    @Resource
    private CoordinatorRegisterHelper publisher;
    
    @Override
    public Object handle(UserPhone myEvent, RunnerMode runnerMode)
    {
        System.out.println("asdasd");
        System.out.println("用户:" + myEvent.getPhone() + "欠费");
        return null;
    }
    
    @Override
    public int getOrder()
    {
        return 0;
    }
    
    @Override
    public void afterContextInit()
    {
        UserPhone phone = new UserPhone();
        phone.setPhone("1775032");
        CoordinatorBuses.computation().post(SmsEvent.halt, this, phone).await();
    }
    
}
