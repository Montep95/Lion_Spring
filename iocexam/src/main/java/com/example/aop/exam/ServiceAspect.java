package com.example.aop.exam;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

// Pointcut + Advice
@Aspect
@Component
public class ServiceAspect {
    @Pointcut("execution(* com.example.aop.exam.*Service.*(..))")
    public void pc(){

    }

    // "execution(리턴타입 패키지경로.클래스이름.메소드이름(매개변수))
    // exam패키지만 포함, SimpleService클래스의 모든메소드(*)를 사용하며 매개변수도 상관없음(..)
    @Before("execution(* com.example.aop.exam.*Service.*(..))")
//    @Before("execution(* com.example..)"); // example패키지 안의 다른 패키지들도 포함
    public void before(JoinPoint joinPoint){// before 매개변수로는 Joinpoint를 받아낼 수 있음
        System.out.println("Before의 메소드(before()) 실행 :::::::::::::: " + joinPoint.getSignature().getName());
    }

    @After("execution(* com.example.aop.exam.*Service.*(..))")
    public void after(){
        System.out.println("After의 메소드 (after()) 실행 :::::::::::::: ");
    }

    // After와의차이점은 After Returning은 예외발생 시 실행되지 않지만 After는 조인포인트 처리 완료 후 항상 실행
    @AfterReturning(pointcut = "pc()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result){ // result는 어떤 값이 올지 모르므로 Object타입으로 받음
        System.out.println("AfterReturning 실행 :::::::::::::: " + joinPoint.getSignature().getName() + ", return value : " + result);
    }

    // 예외가 발생함으로써 AfterReturning은 실행되지 않고 After는 실행됨을 알 수 있다.
    // SimpleService.hello()에서 if(1==1) throw new RuntimeException()을 발생시켰다.
    @AfterThrowing(value = "pc()", throwing = "ex")
    public void afterThrowing(Throwable ex){
        System.out.println("AfterThrowing 실행 :::::::::::::: ");
        System.out.println("exception value : " + ex);
    }

    // ProceedingJoinPoint : JoinPoint를 받아서 다음으로 넘겨주는 역할
    @Around("pc()")
    public String around(ProceedingJoinPoint pjp) throws Throwable{
        System.out.println("Around :::::::::::::: 실제 메소드가 [실행되기 전] 해야할 일");

        String value = (String)pjp.proceed(); // 이 proceed()를 호출해줘야지만 실제 Target의 메소드를 호출한다. -- 이 줄을 기준으로 윗 부분은 실제 메소드가 실행되기 전, 아랫 부분이 시점이 달라짐
        System.out.println("Around :::::::::::::: 실제 메소드가 [실행된 후] 해야할 일");
        value += "Test AOP Run!!";

        return value;
    }

    @Before("execution(* setName(..)*")
    public void beforeSetName(JoinPoint joinPoint){
        System.out.println("beforeSetName ::::::::::");
        System.out.println("target method name :::::::::: " + joinPoint.getSignature().getName());

        Object[] args = joinPoint.getArgs();

        for(Object object : args){
            System.out.println("setName 메소드의 인자 :::::::::: " + object);
        }
    }
}
