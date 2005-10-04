package javolution.realtime;

import j2me.io.Serializable;
import javolution.lang.Reference;
import javolution.util.FastMap;

/**
 * <p> This class represents a reference whose setting is local to the current 
 *     {@link LocalContext}; setting outside of any {@link LocalContext} scope 
 *     affects the reference default value (equivalent to {@link #setDefault}).
 *     For example:<pre>
 *     import org.jscience.physics.units.*;
 *     public class Length {
 *         private static final LocalReference&lt;Unit&gt; OUTPUT_UNIT 
 *             = new LocalReference&lt;Unit&gt;(SI.METER); // Default value.
 *             
 *         // Sets of output unit for Length instances.
 *         public static showAs(Unit unit) {
 *             OUTPUT_UNIT.set(unit);
 *         }
 *         public Text toText() {
 *              return QuantityFormat.getInstance().format(this, OUTPUT_UNIT.get());
 *         }     
 *     }
 *     ...
 *     public static void main(String[] args) {
 *          // Sets the default length output unit to Inches.
 *          Length.showAs(NonSI.INCH); //  Affects all threads.
 *     }
 *     ...
 *     Vector result ... // Shows result with Length stated in kilometers.
 *     LocalContext.enter();
 *     try {
 *        Length.showAs(SI.KILO(SI.METER)); // Affects the local thread only.
 *        System.out.println(result);
 *     } finally {
 *        LocalContext.exit();
 *     }</pre></p>
 *     
 * <p> Accessing/setting a local reference is fast and does not require 
 *     any form of synchronization. Local settings are inherited by 
 *     {@link ConcurrentThread concurrent threads} spawned from within the 
 *     same {@link LocalContext}.</p>
 */
public class LocalReference/*<T>*/implements Reference/*<T>*/, Serializable {

    /**
     * Holds the default value for this reference.
     */
    private Object/*T*/_defaultValue;

    /**
     * Indicates if this reference value has ever been locally overriden 
     * (optimization, most applications use default values).
     */
    private boolean _hasBeenLocallyOverriden;

    /**
     * Default constructor (default referent is <code>null</code>).
     */
    public LocalReference() {
        this(null);
    }

    /**
     * Creates a local reference having the specified default value.
     * 
     * @param defaultValue the default value or root value of this variable.
     */
    public LocalReference(Object/*T*/defaultValue) {
        _defaultValue = defaultValue;
    }

    /**
     * Returns the local value for this reference.
     * The first outer {@link LocalContext} is searched first, then
     * all outer {@link LocalContext} are recursively searched up to the
     * global root context which contains the default value.
     *
     * @return the context-local value.
     */
    public final Object/*T*/get() {
        return (_hasBeenLocallyOverriden) ? retrieveValue() : _defaultValue;
    }

    private Object/*T*/retrieveValue() {
        for (LocalContext ctx = LocalContext.currentLocalContext(); ctx != null; ctx = ctx
                .getOuter().inheritedLocalContext) {
            Object value = ctx._references.get(this);
            if (value != null) {
                return (Object/*T*/) value;
            }
        }
        // Not found, returns default value.
        return _defaultValue;
    }

    /**
     * Sets the local value (referent) for this reference.
     *
     * @param value the new local value or <code>null</code> to inherit
     *        the outer value.
     */
    public void set(Object/*T*/value) {
        LocalContext ctx = LocalContext.currentLocalContext();
        if (ctx != null) {
            FastMap references = ctx._references;
            references.put(this, value);
            _hasBeenLocallyOverriden = true;
            return;
        }
        // No local context, sets default value.
        _defaultValue = value;
    }

    /**
     * Returns the default value for this reference.
     *
     * @return the defaultValue.
     */
    public Object/*T*/getDefault() {
        return _defaultValue;
    }

    /**
     * Returns the local (non-inherited) value for this reference.
     *
     * @return the local value or <code>null</code> if none (value to be 
     *         inherited or not set).
     */
    public Object/*T*/getLocal() {
        LocalContext ctx = LocalContext.currentLocalContext();
        return (ctx != null) ? (Object/*T*/) ctx._references.get(this)
                : _defaultValue;
    }

    /**
     * Sets the default value for this reference.
     *
     * @param  defaultValue the root value.
     */
    public void setDefault(Object/*T*/defaultValue) {
        _defaultValue = defaultValue;
    }
}