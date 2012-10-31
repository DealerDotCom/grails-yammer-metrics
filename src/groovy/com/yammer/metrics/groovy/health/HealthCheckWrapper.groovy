package com.yammer.metrics.groovy.health

import com.yammer.metrics.core.HealthCheck

import com.yammer.metrics.HealthChecks
import java.util.concurrent.TimeUnit
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

/**
 * Subclass of com.yammer.metrics.core.HealthCheck that wraps a closure and executes with a timeout. If the closure doesn't return a value in the time
 * specified, the check will fail.
 *
 * Usage examples:
 *
    HealthCheckWrapper.register('my-check'){ myService.someCheck() }           // Register myService.someCheck() as a health check with a 1 second timeout

    HealthCheckWrapper.register('long-check', 3L){ myService.someCheck() }     // Same as above, but with a 3 second timeout

    HealthCheckWrapper.register('short-check', 200L, TimeUnit.MILISECONDS){    // Same as above, but with a 200 ms timeout
        myService.someCheck()
    }

    HealthCheckWrapper.register('custom-check'){                               // Custom HealthCheck.Result return value
        myService.someCheck() ?
            HealthCheck.Result.healthy() :
            HealthCheck.Result.unhealthy('Bad check')
    }

    HealthCheckWrapper.register('string-check'){                               // Returning a string will be transformed into unhealthy message
        myService.someCheck() ?: 'Bad Check'
    }

    HealthCheckWrapper.register('exception-check'){                            // A thrown exception will be transformed into unhealthy result
        throw new RuntimeException('Bad Check')
    }
 *
 */
class HealthCheckWrapper extends HealthCheck{

    private final Closure checkClosure
    private final String checkName

    private final ExecutorService executorService

    private final long timeout
    private final TimeUnit unit

    /**
     * Simple constructor, uses a default timeout of 1 Second.
     * @param theCheckName Name of the check
     * @param theCheck Closure that is invoked to perform the check. Should either return a Boolean (true is healthy, false is unhealthy),
     *                  a String (description of an unhealthy check) or a HealthCheck.Result instance.
     */
    public HealthCheckWrapper(String theCheckName, Closure theCheck){
        this(theCheckName, 1L, TimeUnit.SECONDS, theCheck)
    }

    /**
     * Constructor that lets you specify the timeout and unit for the check.
     * @param theCheckName Name of the check
     * @param timeout The timeout value for the health check
     * @param unit The time unit for the timeout
     * @param theCheck Closure that is invoked to perform the check. Should either return a Boolean (true is healthy,
     *                  false is unhealthy), a String (description of an unhealthy check) or a HealthCheck.Result instance.
     */
    public HealthCheckWrapper(String theCheckName, Long timeout, TimeUnit unit, Closure theCheck){
        super(theCheckName)
        this.checkClosure = theCheck
        this.checkName = theCheckName
        this.timeout = timeout
        this.unit = unit
        this.executorService = Executors.newSingleThreadExecutor()
    }

    @Override
    protected HealthCheck.Result check(){
        try{
            def checkReturnValue = executorService.submit(checkClosure as Callable).get(timeout, unit)
            if(checkReturnValue instanceof HealthCheck.Result){
                return (HealthCheck.Result) checkReturnValue
            }
            if(checkReturnValue instanceof Boolean){
                return checkReturnValue ? HealthCheck.Result.healthy() : HealthCheck.Result.unhealthy("$checkName health check returned false")
            }
            if(checkReturnValue instanceof String){
                return HealthCheck.Result.unhealthy(checkReturnValue)
            }
            return HealthCheck.Result.unhealthy("$checkName health check returned $checkReturnValue".toString())
        } catch(java.util.concurrent.TimeoutException e){
            return HealthCheck.Result.unhealthy("$checkName health check timed out after $timeout ${unit.toString().toLowerCase()}")
        } catch(Throwable e){
            return HealthCheck.Result.unhealthy(e)
        }
    }

    /**
     * Convenience method for registering a new HealthCheckWrapper instance with a timeout of 1 second.
     * @param name Name of the check
     * @param theCheck Closure that executes the check
     */
    static void register(String name, Closure theCheck){
        register(name, 1L, theCheck)
    }

    /**
     * Convenience method for registering a new HealthCheckWrapper instance with a timeout specifying the number of
     * seconds to wait before failing.
     * @param name Name of the check
     * @param timeout Number of seconds to wait before timing out
     * @param theCheck Closure that executes the check
     */
    static void register(String name, Long timeout, Closure theCheck){
        register(name, timeout, TimeUnit.SECONDS, theCheck)
    }

    /**
     * Convenience method for registering a new HealthCheckWrapper instance with a custom timeout value and unit.
     * @param name Name of the check
     * @param timeout The timeout value for the health check
     * @param unit The time unit for the timeout
     * @param theCheck Closure that executes the check
     */
    static void register(String name, Long timeout, TimeUnit unit, Closure theCheck){
        HealthChecks.register(new HealthCheckWrapper(name, timeout, unit, theCheck))
    }
}
