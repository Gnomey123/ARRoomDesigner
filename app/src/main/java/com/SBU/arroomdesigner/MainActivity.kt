package com.SBU.arroomdesigner


import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.getDescription
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.utils.doOnApplyWindowInsets
import io.github.sceneview.utils.setFullScreen





class MainActivity : AppCompatActivity(R.layout.activity_main) {

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var statusText: TextView
    lateinit var placeModelButton: ExtendedFloatingActionButton
    lateinit var deleteAllObjectsButton: ExtendedFloatingActionButton

    lateinit var rotateSeekBar: SeekBar

    lateinit var placeChairButton: Button


    lateinit var placeLamp: Button


    lateinit var placeSpoonButton: Button


    lateinit var placeTea: Button


    lateinit var placeDesk: Button

    lateinit var placeClock: Button

    lateinit var placeRack: Button

    private var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }


    data class Model(
        val fileLocation: String,
        val scaleUnits: Float? = null,
        val placementMode: PlacementMode = PlacementMode.BEST_AVAILABLE,
        val applyPoseRotation: Boolean = true
    )

    val models = listOf(
        Model("chair.glb"),
        Model(
            fileLocation = "https://sceneview.github.io/assets/models/Spoons.glb",
            placementMode = PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL,
            // Keep original model size
            scaleUnits = null
        ),
        Model("teapot.glb"),
        Model("clock.glb"),
        Model("desk.glb"),
        Model("lamp.glb"),
        Model("rack.glb"),
    )
    var modelIndex = 0
    var modelNode: ArModelNode? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFullScreen(
            findViewById(R.id.rootView),
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar)?.apply {
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).topMargin = systemBarsInsets.top
            }
            title = ""
        })
        statusText = findViewById(R.id.statusText)
        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            onArTrackingFailureChanged = { reason ->
                statusText.text = reason?.getDescription(context)
                statusText.isGone = reason == null
            }
        }


        //enable cloud ankoering
        sceneView.apply {
            cloudAnchorEnabled = true
        }


        loadingView = findViewById(R.id.loadingView)
        deleteAllObjectsButton =
            findViewById<ExtendedFloatingActionButton>(R.id.deleteObjects).apply {

                setOnClickListener { deleteArObject() }
            }
        placeModelButton = findViewById<ExtendedFloatingActionButton>(R.id.placeModelButton).apply {
            setOnClickListener { placeModelNode() }
        }


        placeChairButton = findViewById<Button>(R.id.chairButton).apply {
            setOnClickListener { placeChair() }
        }

        placeTea = findViewById<Button>(R.id.teaButton).apply {
            setOnClickListener { placeTea() }
        }

        placeClock = findViewById<Button>(R.id.clockButton).apply {
            setOnClickListener { placeClock() }
        }

        placeDesk = findViewById<Button>(R.id.deskButton).apply {
            setOnClickListener { placeDesk() }
        }


        placeLamp = findViewById<Button>(R.id.lampButton).apply {
            setOnClickListener { placeLamp() }
        }


        placeRack = findViewById<Button>(R.id.rackButton).apply {
            setOnClickListener { placeRack() }
        }




        placeSpoonButton = findViewById<Button>(R.id.spoonButton).apply {
            setOnClickListener { placeSpoon() }
        }


        //code to make rotateSeekBar as tall as the AR view fragment
        rotateSeekBar = findViewById<SeekBar>(R.id.rotateSeekBar)

        sceneView.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            rotateSeekBar.layoutParams.width = ((bottom - top) * 0.8).toInt();
            sceneView.requestLayout();
        }

        rotateSeekBar.layoutParams.width = ((sceneView.bottom - sceneView.top) * 0.8).toInt();
        sceneView.requestLayout();






        rotateSeekBar?.max = 360;

        rotateSeekBar.min = 0;




        rotateSeekBar?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                rotateSeekBar: SeekBar,
                progress: Int, fromUser: Boolean
            ) {
                // write custom code for progress is changed
            }

            override fun onStartTrackingTouch(rotateSeekBar: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(rotateSeekBar: SeekBar) {
                // write custom code for progress is stopped
                if (sceneView.selectedNode != null)  //only rotate if we've selected something
                {

                    var rotateVal = rotateSeekBar.progress


                    sceneView.selectedNode?.apply {

                        transform(rotation = Rotation(y = rotateVal.toFloat()))

                    }

                }

            }
        })


    }



    var placeOption = 0







    fun placeChair()
    {

        placeOption = 0;

        newModelNode()
    }


    fun placeSpoon()
    {

        placeOption = 1;

        newModelNode()
    }


    fun placeTea()
    {
        placeOption = 2;

        newModelNode()

    }

    fun placeClock()
    {
        placeOption = 3;

        newModelNode()

    }

    fun placeDesk()
    {
        placeOption = 4;

        newModelNode()

    }

    fun placeLamp()
    {
        placeOption = 5;

        newModelNode()

    }


    fun placeRack()
    {
        placeOption = 6;

        newModelNode()

    }




    fun placeModelNode() {
        modelNode?.anchor()
        placeModelButton.isVisible = false
        sceneView.planeRenderer.isVisible = false


    }
    var modelList = mutableListOf<ArModelNode>()

    fun newModelNode()
    {

        modelNode?.placementMode = PlacementMode.BEST_AVAILABLE;


        isLoading = true

        modelNode?.takeIf { !it.isAnchored }?.let {
            sceneView.removeChild(it)
            it.destroy()
        }
        val model = models[placeOption]

        modelNode = ArModelNode(model.placementMode).apply {
            //applyPoseRotation = model.applyPoseRotation
            loadModelGlbAsync(
                glbFileLocation = model.fileLocation,
                autoAnimate = true,
                scaleToUnits = model.scaleUnits,
                // Place the model origin at the bottom center
                centerOrigin = Position(y = -1.0f)
            ) {
                sceneView.planeRenderer.isVisible = true
                isLoading = false
            }
            onAnchorChanged = { anchor ->
                placeModelButton.isGone = anchor != null
            }


            onHitResult = { node, _ ->
                placeModelButton.isGone = !node.isTracking
            }





        }



        sceneView.addChild(modelNode!!)
        // Select the model node by default (the model node is also selected on tap)
        sceneView.selectedNode = modelNode








    }





    fun deleteArObject()
    {


        if(sceneView.selectedNode == null)
        {
            val toast = Toast.makeText(applicationContext, "Can't delete, no objects selected", Toast.LENGTH_SHORT)
            toast.show()
            return
        }

        sceneView.removeChild(sceneView.selectedNode!!)

    }


}
