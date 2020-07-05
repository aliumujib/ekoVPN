/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.ripple

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Paint
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import com.ekovpn.android.R

class RippleAnimationView : RelativeLayout {
    private var rippleColor = 0
    private var rippleStrokeWidth = 0f
    private var rippleRadius = 0f
    private var rippleDurationTime = 0
    private var rippleAmount = 0
    private var rippleDelay = 0
    private var rippleScale = 0f
    private var rippleType = 0
    private var paint: Paint? = null

    //stopping ripple Animation using end() method and setting boolean value false to animationRunning
    /*call this method to check whether animation is running or not.
    * then basically it return the animationRunning boolean value.
    *
    * even we have stopped animation then it will return false value.
    * or even we have not stopped animation then it will return true value.
    */
    var isRippleAnimationRunning = false
        private set
    private var animatorSet: AnimatorSet? = null
    private var animatorList: ArrayList<Animator>? = null
    private var rippleParams: LayoutParams? = null
    private val rippleViewList = ArrayList<RippleView>()

    //constructor with single parameter
    constructor(context: Context?) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    //Parameter Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    //initialized and sets all the properties of RippleAnimation
    private fun init(context: Context, attrs: AttributeSet?) {
        if (isInEditMode) return
        requireNotNull(attrs) { "Attributes should be provided to this view," }

        //created typedArray and setting all properties to typedArray object
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleAnimation)

        //it will return ripple color even we have specified into xml or else it will get the default ripple Color
        rippleColor = typedArray.getColor(R.styleable.RippleAnimation_rb_color, resources.getColor(R.color.eko_red))

        //getting rippleStrokeWidth from typedArray object and putting it into rippleStrokeWidth variable
        rippleStrokeWidth = typedArray.getDimension(R.styleable.RippleAnimation_rb_strokeWidth, resources.getDimension(R.dimen.one_space))

        //rippleRadius contains the default the specified radius even we specified or else it takes default radius...which is 64dp
        rippleRadius = typedArray.getDimension(R.styleable.RippleAnimation_rb_radius, resources.getDimension(R.dimen.ripple_radius))

        //setting rippleAnimation duration is specified or else it takes default Duration Time.
        rippleDurationTime = typedArray.getInt(R.styleable.RippleAnimation_rb_duration, DEFAULT_DURATION_TIME)

        //number of ripple if we have specified into design part or else default value it will take.
        rippleAmount = typedArray.getInt(R.styleable.RippleAnimation_rb_rippleAmount, DEFAULT_RIPPLE_COUNT)

        //setting scale of ripple
        rippleScale = typedArray.getFloat(R.styleable.RippleAnimation_rb_scale, DEFAULT_SCALE)

        //its getting the ripple type and putting it into rippleType variable.
        rippleType = typedArray.getInt(R.styleable.RippleAnimation_rb_type, DEFAULT_FILL_TYPE)
        /**
         * Recycles the TypedArray, to be re-used by a later caller. After calling
         * this function you must not ever touch the typed array again.
         *
         * @throws RuntimeException if the TypedArray has already been recycled.
         */
        typedArray.recycle()
        rippleDelay = rippleDurationTime / rippleAmount
        paint = Paint() //creating an object of paint to make ripple based on the ripple type
        paint?.isAntiAlias = true
        if (rippleType == DEFAULT_FILL_TYPE) {
            rippleStrokeWidth = 0f
            paint?.style = Paint.Style.FILL
        } else paint?.style = Paint.Style.STROKE
        paint?.color = rippleColor
        rippleParams = LayoutParams((2 * (rippleRadius + rippleStrokeWidth)).toInt(), (2 * (rippleRadius + rippleStrokeWidth)).toInt())
        rippleParams?.addRule(CENTER_IN_PARENT, TRUE) //setting ripple into center
        animatorSet = AnimatorSet() // creates animationSet object
        animatorSet?.interpolator = AccelerateDecelerateInterpolator()
        animatorList = ArrayList()


        /*based on the number of ripple this for loop create ripple View and put three different animation for this view..
        *
        * */for (i in 0 until rippleAmount) {
            val rippleView = RippleView(getContext()) //created ripple
            addView(rippleView, rippleParams) //adding view
            rippleViewList.add(rippleView) //putting this view into rippleViewList object


            //creating objectAnimator object and setting some X Scale properties to object and putting it into animatorList<Animator>
            val scaleXAnimator: ObjectAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleX", 1.0f, rippleScale)
            scaleXAnimator.repeatCount = ObjectAnimator.INFINITE
            scaleXAnimator.repeatMode = ObjectAnimator.RESTART
            scaleXAnimator.startDelay = (i * rippleDelay).toLong()
            scaleXAnimator.duration = rippleDurationTime.toLong()
            animatorList!!.add(scaleXAnimator)

            //creating objectAnimator object and setting some Y Scale properties to object and putting it into animatorList<Animator>
            val scaleYAnimator: ObjectAnimator = ObjectAnimator.ofFloat(rippleView, "ScaleY", 1.0f, rippleScale)
            scaleYAnimator.repeatCount = ObjectAnimator.INFINITE
            scaleYAnimator.repeatMode = ObjectAnimator.RESTART
            scaleYAnimator.startDelay = (i * rippleDelay).toLong()
            scaleYAnimator.duration = rippleDurationTime.toLong()
            animatorList!!.add(scaleYAnimator)

            //setting alpha of the animation and putting it into animatorList<Animator>
            val alphaAnimator: ObjectAnimator = ObjectAnimator.ofFloat(rippleView, "Alpha", 1.0f, 0f)
            alphaAnimator.repeatCount = ObjectAnimator.INFINITE
            alphaAnimator.repeatMode = ObjectAnimator.RESTART
            alphaAnimator.startDelay = (i * rippleDelay).toLong()
            alphaAnimator.duration = rippleDurationTime.toLong()
            animatorList!!.add(alphaAnimator)
        }

        //played animator
        animatorSet?.playTogether(animatorList)
    }

    //used to start ripple Animation
    fun startRippleAnimation() {
        if (!isRippleAnimationRunning) {
            for (rippleView in rippleViewList) {
                rippleView.visibility = VISIBLE
            }
            animatorSet?.start() //animation started
            isRippleAnimationRunning = true //setting value as true... this will support to check isRippleAnimation is running or not.
        }
    }

    fun stopRippleAnimation() {
        if (isRippleAnimationRunning) {
            animatorSet?.end()
            isRippleAnimationRunning = false
        }
    }

    //this class is using to draw the circle
    private inner class RippleView(context: Context?) : View(context) {
        //it's drawing ripple based on radius value.
        protected override fun onDraw(canvas: Canvas) {
            val radius = Math.min(getWidth(), getHeight()) / 2
            paint?.let { canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius - rippleStrokeWidth, it) }
        }

        init {
            this.visibility = INVISIBLE
        }
    }

    companion object {
        private const val DEFAULT_RIPPLE_COUNT = 6
        private const val DEFAULT_DURATION_TIME = 3000
        private const val DEFAULT_SCALE = 6.0f
        private const val DEFAULT_FILL_TYPE = 0
    }
}