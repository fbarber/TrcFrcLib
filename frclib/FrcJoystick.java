/*
 * Copyright (c) 2015 Titan Robotics Club (http://www.titanrobotics.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package TrcFrcLib.frclib;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import TrcCommonLib.trclib.TrcDbgTrace;
import TrcCommonLib.trclib.TrcRobot;
import TrcCommonLib.trclib.TrcTaskMgr;
import TrcCommonLib.trclib.TrcTimer;
import TrcCommonLib.trclib.TrcUtil;

/**
 * This class implements the platform dependent joystick. It provides monitoring of the joystick buttons. If the
 * caller of this class provides a button notification handler, it will call it when there are button events.
 */
public class FrcJoystick extends Joystick
{
    private static final TrcDbgTrace globalTracer = TrcDbgTrace.getGlobalTracer();
    private static final boolean debugEnabled = false;

    //
    // Logitech Joystick:
    // UsagePage=0x01, Usage=0x04
    //
    public static final int LOGITECH_TRIGGER    = 1;
    public static final int LOGITECH_BUTTON2    = 2;
    public static final int LOGITECH_BUTTON3    = 3;
    public static final int LOGITECH_BUTTON4    = 4;
    public static final int LOGITECH_BUTTON5    = 5;
    public static final int LOGITECH_BUTTON6    = 6;
    public static final int LOGITECH_BUTTON7    = 7;
    public static final int LOGITECH_BUTTON8    = 8;
    public static final int LOGITECH_BUTTON9    = 9;
    public static final int LOGITECH_BUTTON10   = 10;
    public static final int LOGITECH_BUTTON11   = 11;
    public static final int LOGITECH_BUTTON12   = 12;
    //
    // Logitech DualAction Game Controller:
    // UsagePage=0x01, Usage=0x04
    //
    public static final int DUALACTION_BUTTONX  = 1;
    public static final int DUALACTION_BUTTONA  = 2;
    public static final int DUALACTION_BUTTONB  = 3;
    public static final int DUALACTION_BUTTONY  = 4;
    public static final int DUALACTION_LB       = 5;
    public static final int DUALACTION_RB       = 6;
    public static final int DUALACTION_LT       = 7;
    public static final int DUALACTION_RT       = 8;
    public static final int DUALACTION_BACK     = 9;
    public static final int DUALACTION_START    = 10;
    public static final int DUALACTION_LTOP     = 11;
    public static final int DUALACTION_RTOP     = 12;
    //
    // Microsoft SideWinder Joystick:
    // UsagePage=0x01, Usage=0x04
    //
    public static final int SIDEWINDER_TRIGGER  = 1;
    public static final int SIDEWINDER_BUTTON2  = 2;
    public static final int SIDEWINDER_BUTTON3  = 3;
    public static final int SIDEWINDER_BUTTON4  = 4;
    public static final int SIDEWINDER_BUTTON5  = 5;
    public static final int SIDEWINDER_BUTTON6  = 6;
    public static final int SIDEWINDER_BUTTON7  = 7;
    public static final int SIDEWINDER_BUTTON8  = 8;
    public static final int SIDEWINDER_BUTTON9  = 9;
    //
    // Generic USB Button Panel:
    // UsagePage=0x01, Usage=0x04 (i have no idea what this means)
    //
    public static final int PANEL_BUTTON_RED1   = 1;
    public static final int PANEL_BUTTON_GREEN1 = 2;
    public static final int PANEL_BUTTON_BLUE1  = 3;
    public static final int PANEL_BUTTON_YELLOW1= 4;
    public static final int PANEL_BUTTON_WHITE1 = 5;
    public static final int PANEL_BUTTON_RED2   = 6;
    public static final int PANEL_BUTTON_GREEN2 = 7;
    public static final int PANEL_BUTTON_BLUE2  = 8;
    public static final int PANEL_BUTTON_YELLOW2= 9;
    public static final int PANEL_BUTTON_WHITE2 = 10;
    //
    // Generic USB Switch Panel:
    // UsagePage=0x01, Usage=0x04 (i have no idea what this means)
    //
    public static final int PANEL_SWITCH_WHITE1 = 1;
    public static final int PANEL_SWITCH_RED1   = 2;
    public static final int PANEL_SWITCH_GREEN1 = 3;
    public static final int PANEL_SWITCH_BLUE1  = 4;
    public static final int PANEL_SWITCH_YELLOW1= 5;
    public static final int PANEL_SWITCH_WHITE2 = 6;
    public static final int PANEL_SWITCH_RED2   = 7;
    public static final int PANEL_SWITCH_GREEN2 = 8;
    public static final int PANEL_SWITCH_BLUE2  = 9;
    public static final int PANEL_SWITCH_YELLOW2= 10;

    private static final double DEF_DEADBAND_THRESHOLD = 0.15;
    private static final double DEF_SAMPLING_PERIOD = 0.02;     //Sampling at 50Hz.
    private double samplingPeriod = DEF_SAMPLING_PERIOD;
    private double nextPeriod = 0.0;
    private double deadbandThreshold = DEF_DEADBAND_THRESHOLD;

    private final String instanceName;
    private final int port;
    private final TrcTaskMgr.TaskObject buttonEventTaskObj;
    private int prevButtons;
    private FrcButtonHandler buttonHandler = null;
    private int ySign = 1;
    private TrcDbgTrace buttonEventTracer = null;

    /**
     * Constructor: Create an instance of the object.
     *
     * @param instanceName specifies the instance name.
     * @param port         specifies the joystick port ID.
     */
    public FrcJoystick(String instanceName, int port)
    {
        super(port);

        this.instanceName = instanceName;
        this.port = port;
        buttonEventTaskObj = TrcTaskMgr.createTask(instanceName + ".buttonEvent", this::buttonEventTask);
        prevButtons = DriverStation.getStickButtons(port);
    }   //FrcJoystick

    /**
     * Constructor: Create an instance of the object.
     *
     * @param instanceName      specifies the instance name.
     * @param port              specifies the joystick port ID.
     * @param deadbandThreshold specifies the deadband of the analog sticks.
     */
    public FrcJoystick(final String instanceName, final int port, final double deadbandThreshold)
    {
        this(instanceName, port);
        this.deadbandThreshold = deadbandThreshold;
    }   //FrcJoystick

    /**
     * This method returns the instance name.
     *
     * @return instance name.
     */
    public String toString()
    {
        return instanceName;
    }   //toString

    /**
     * This method enables/disables button event tracing by setting the event tracer.
     *
     * @param buttonEventTracer specifies the tracer to use when enabling button events tracing, null to disable
     *        event tracing.
     */
    public void setButtonEventTracer(TrcDbgTrace buttonEventTracer)
    {
        this.buttonEventTracer = buttonEventTracer;
    }   //setButtonEventTracer

    /**
     * This method sets the object that will handle button events. Any previous handler set with this method will
     * no longer receive events.
     *
     * @param buttonHandler specifies the object that will handle the button events. Set to null clear previously
     *                      set handler.
     */
    public void setButtonHandler(FrcButtonHandler buttonHandler)
    {
        this.buttonHandler = buttonHandler;
        if (buttonHandler != null)
        {
            buttonEventTaskObj.registerTask(TrcTaskMgr.TaskType.PRE_PERIODIC_TASK);
        }
        else
        {
            buttonEventTaskObj.unregisterTask();
        }
    }   //setButtonHandler

    /**
     * This method returns the current button event handler.
     *
     * @return current button event handler, null if none.
     */
    public FrcButtonHandler getButtonHandler()
    {
        return buttonHandler;
    }   //getButtonHandler

    /**
     * This method sets the joystick button sampling period. By default, it is sampling at 50Hz. One could change
     * the sampling period by calling this method.
     *
     * @param period specifies the new sampling period in seconds.
     */
    public void setSamplingPeriod(double period)
    {
        samplingPeriod = period;
    }   //setSamplingPeriod

    /**
     * This method inverts the y-axis of the analog sticks.
     *
     * @param inverted specifies true if inverting the y-axis, false otherwise.
     */
    public void setYInverted(boolean inverted)
    {
        ySign = inverted ? -1 : 1;
    }   //setYInverted

    /**
     * This method returns the value of the X analog stick.
     *
     * @param squared           specifies true to apply a squared curve to the output value, false otherwise.
     * @param deadbandThreshold specifies the deadband value to apply on this stick.
     * @return adjusted value of the X analog stick.
     */
    public double getXWithDeadband(boolean squared, double deadbandThreshold)
    {
        return adjustValueWithDeadband(getX(), squared, deadbandThreshold);
    }   //getXWithDeadband

    /**
     * This method returns the value of the X analog stick.
     *
     * @param squared specifies true to apply a squared curve to the output value, false otherwise.
     * @return adjusted value of the X analog stick.
     */
    public double getXWithDeadband(boolean squared)
    {
        return getXWithDeadband(squared, deadbandThreshold);
    }   //getXWithDeadband

    /**
     * This method returns the value of the Y analog stick.
     *
     * @param squared           specifies true to apply a squared curve to the output value, false otherwise.
     * @param deadbandThreshold specifies the deadband value to apply on this stick.
     * @return adjusted value of the Y analog stick.
     */
    public double getYWithDeadband(boolean squared, double deadbandThreshold)
    {
        return adjustValueWithDeadband(ySign * getY(), squared, deadbandThreshold);
    }   //getYWithDeadband

    /**
     * This method returns the value of the Y analog stick.
     *
     * @param squared specifies true to apply a squared curve to the output value, false otherwise.
     * @return adjusted value of the Y analog stick.
     */
    public double getYWithDeadband(boolean squared)
    {
        return getYWithDeadband(squared, deadbandThreshold);
    }   //getYWithDeadband

    /**
     * This method returns the value of the Z analog stick.
     *
     * @param squared           specifies true to apply a squared curve to the output value, false otherwise.
     * @param deadbandThreshold specifies the deadband value to apply on this stick.
     * @return adjusted value of the Z analog stick.
     */
    public double getZWithDeadband(boolean squared, double deadbandThreshold)
    {
        return adjustValueWithDeadband(getZ(), squared, deadbandThreshold);
    }   //getZWithDeadband

    /**
     * This method returns the value of the Z analog stick.
     *
     * @param squared specifies true to apply a squared curve to the output value, false otherwise.
     * @return adjusted value of the Z analog stick.
     */
    public double getZWithDeadband(boolean squared)
    {
        return getZWithDeadband(squared, deadbandThreshold);
    }   //getZWithDeadband

    /**
     * This method returns the value of the Twist analog stick.
     *
     * @param squared           specifies true to apply a squared curve to the output value, false otherwise.
     * @param deadbandThreshold specifies the deadband value to apply on this stick.
     * @return adjusted value of the Twist analog stick.
     */
    public double getTwistWithDeadband(boolean squared, double deadbandThreshold)
    {
        return adjustValueWithDeadband(getTwist(), squared, deadbandThreshold);
    }   //getTwistWithDeadband

    /**
     * This method returns the value of the Twist analog stick.
     *
     * @param squared specifies true to apply a squared curve to the output value, false otherwise.
     * @return adjusted value of the Twist analog stick.
     */
    public double getTwistWithDeadband(boolean squared)
    {
        return getTwistWithDeadband(squared, deadbandThreshold);
    }   //getTwistWithDeadband

    /**
     * This method returns the value of the analog Throttle.
     *
     * @param squared           specifies true to apply a squared curve to the output value, false otherwise.
     * @param deadbandThreshold specifies the deadband value to apply on this stick.
     * @return adjusted value of the analog Throttle.
     */
    public double getThrottleWithDeadband(boolean squared, double deadbandThreshold)
    {
        return adjustValueWithDeadband(getThrottle(), squared, deadbandThreshold);
    }   //getThrottleWithDeadband

    /**
     * This method returns the value of the analog Throttle.
     *
     * @param squared specifies true to apply a squared curve to the output value, false otherwise.
     * @return adjusted value of the analog Throttle.
     */
    public double getThrottleWithDeadband(boolean squared)
    {
        return getThrottleWithDeadband(squared, deadbandThreshold);
    }   //getThrottleWithDeadband

    /**
     * This method returns the value of the analog magnitude.
     *
     * @param squared           specifies true to apply a squared curve to the output value, false otherwise.
     * @param deadbandThreshold specifies the deadband value to apply on this stick.
     * @return adjusted value of the analog magnitude.
     */
    public double getMagnitudeWithDeadband(boolean squared, double deadbandThreshold)
    {
        return adjustValueWithDeadband(getMagnitude(), squared, deadbandThreshold);
    }   //getMagnitudeWithDeadband

    /**
     * This method returns the value of the analog stick magnitude.
     *
     * @param squared specifies true to apply a squared curve to the output value, false otherwise.
     * @return adjusted value of the analog stick magnitude.
     */
    public double getMagnitudeWithDeadband(boolean squared)
    {
        return getMagnitudeWithDeadband(squared, deadbandThreshold);
    }   //getMagnitudeWithDeadband

    /**
     * This method returns the value of the analog stick direction in radians.
     *
     * @param squared           specifies true to apply a squared curve to the output value, false otherwise.
     * @param deadbandThreshold specifies the deadband value to apply on this stick.
     * @return adjusted value of the analog stick direction in radians.
     */
    public double getDirectionRadiansWithDeadband(boolean squared, double deadbandThreshold)
    {
        return adjustValueWithDeadband(getDirectionRadians(), squared, deadbandThreshold);
    }   //getDirectionRadiansWithDeadband

    /**
     * This method returns the value of the analog stick direction in radians.
     *
     * @param squared specifies true to apply a squared curve to the output value, false otherwise.
     * @return adjusted value of the analog stick direction in radians.
     */
    public double getDirectionRadiansWithDeadband(boolean squared)
    {
        return getDirectionRadiansWithDeadband(squared, deadbandThreshold);
    }   //getDirectionRadiansWithDeadband

    /**
     * This method returns the value of the analog stick direction in degrees.
     *
     * @param squared           specifies true to apply a squared curve to the output value, false otherwise.
     * @param deadbandThreshold specifies the deadband value to apply on this stick.
     * @return adjusted value of the analog stick direction in degrees.
     */
    public double getDirectionDegreesWithDeadband(boolean squared, double deadbandThreshold)
    {
        return adjustValueWithDeadband(getDirectionDegrees(), squared, deadbandThreshold);
    }   //getDirectionDegreesWithDeadband

    /**
     * This method returns the value of the analog stick direction in degrees.
     *
     * @param squared specifies true to apply a squared curve to the output value, false otherwise.
     * @return adjusted value of the analog stick direction in degrees.
     */
    public double getDirectionDegreesWithDeadband(boolean squared)
    {
        return getDirectionDegreesWithDeadband(squared, deadbandThreshold);
    }   //getDirectionDegreesWithDeadband

    /**
     * This method returns the state of the button.
     *
     * @param buttonID specifies the button to check its state.
     * @return true if the button is pressed, false if released.
     */
    public boolean isButtonPressed(int buttonID)
    {
        return (DriverStation.getStickButtons(port) & buttonID) != 0;
    }   //isButtonPressed

    /**
     * This method runs periodically and checks for changes in the button states. If any button changed state,
     * the button handler is called if one exists.
     *
     * @param taskType specifies the type of task being run.
     * @param runMode  specifies the current robot run mode.
     * @param slowPeriodicLoop specifies true if it is running the slow periodic loop on the main robot thread,
     *        false otherwise.
     */
    private void buttonEventTask(TrcTaskMgr.TaskType taskType, TrcRobot.RunMode runMode, boolean slowPeriodicLoop)
    {
        if (slowPeriodicLoop)
        {
            final String funcName = "buttonEventTask";
            double currTime = TrcTimer.getCurrentTime();

            if (currTime >= nextPeriod)
            {
                nextPeriod = currTime + samplingPeriod;

                int currButtons = DriverStation.getStickButtons(port);
                if (buttonHandler != null && runMode != TrcRobot.RunMode.DISABLED_MODE)
                {
                    int changedButtons = prevButtons ^ currButtons;

                    while (changedButtons != 0)
                    {
                        //
                        // buttonMask contains the least significant set bit.
                        //
                        int buttonMask = changedButtons & ~(changedButtons ^ -changedButtons);
                        boolean pressed = (currButtons & buttonMask) != 0;
                        int buttonNum = TrcUtil.leastSignificantSetBitPosition(buttonMask) + 1;

                        if (buttonEventTracer != null)
                        {
                            buttonEventTracer.traceInfo(funcName, "[%.3f] joystick=%s, button=%d, pressed=%b",
                                currTime, instanceName, buttonNum, pressed);
                        }

                        if (pressed)
                        {
                            //
                            // Button is pressed.
                            //
                            if (debugEnabled)
                            {
                                globalTracer.traceInfo(funcName, "Button %x pressed", buttonNum);
                            }
                            buttonHandler.buttonEvent(buttonNum, true);
                        }
                        else
                        {
                            //
                            // Button is released.
                            //
                            if (debugEnabled)
                            {
                                globalTracer.traceInfo(funcName, "Button %x released", buttonNum);
                            }
                            buttonHandler.buttonEvent(buttonNum, false);
                        }
                        //
                        // Clear the least significant set bit.
                        //
                        changedButtons &= ~buttonMask;
                    }
                }
                prevButtons = currButtons;
            }
        }
    }   //buttonEventTask

    /**
     * This method applies deadband to the value and squared the output if necessary.
     *
     * @param value             specifies the value to be processed.
     * @param squared           specifies true to apply a squared curve to the output value, false otherwise.
     * @param deadbandThreshold specifies the deadband value to apply to the value.
     * @return adjusted value.
     */
    private double adjustValueWithDeadband(double value, boolean squared, double deadbandThreshold)
    {
        value = (Math.abs(value) >= deadbandThreshold) ? value : 0.0;

        if (squared)
        {
            value = Math.signum(value) * value * value;
        }

        return value;
    }   //adjustValueWithDeadband

}   //class FrcJoystick
