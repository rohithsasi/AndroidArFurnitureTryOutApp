package com.example.myfirstarfurnitureapp

import android.graphics.Color
import android.media.CamcorderProfile
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myfirstarfurnitureapp.models.FurnitureImage
import com.example.myfirstarfurnitureapp.ui.FurnitureAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {

    private val DOUBLE_TAP_TOLERANCE_MS = 1000L
    private lateinit var arFragment: ArFragment
    private lateinit var selectedModel: FurnitureImage
    private lateinit var videoRecorder: VideoRecorder
    val viewNodes = mutableListOf<Node>()
    private var isRecording = false

    lateinit var photoSaver: PhotoSaver

    private val furnitureModel = mutableListOf(
        FurnitureImage(R.drawable.chair, "Chair", R.raw.chair),
        FurnitureImage(R.drawable.oven, "Oven", R.raw.oven),
        FurnitureImage(R.drawable.piano, "Piano", R.raw.piano),
        FurnitureImage(R.drawable.table, "Table", R.raw.table)
    )

    private val currentScene: Scene
        get() = arFragment.arSceneView.scene

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = view_frag as ArFragment
        photoSaver = PhotoSaver(this)

        videoRecorder = VideoRecorder(this).apply {
            sceneView = arFragment.arSceneView
            setVideoQuality(CamcorderProfile.QUALITY_1080P, resources.configuration.orientation)
        }

        setUpBottomSheet()
        setUpRecycleView()
        setUpDoubleTapArPlanelistener()
        setupFab()


        currentScene.addOnUpdateListener {
            rotateViewNodes()
        }
    }

    private fun setUpBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics)
                .toInt()
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bottomSheet.bringToFront()
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {

            }
        })
    }

    private fun setupFab() {
        fab.setOnClickListener {
            photoSaver.takePhoto(arFragment.arSceneView)
        }

        fab.setOnLongClickListener {
            isRecording = videoRecorder.toggleRecordingState()
            true
        }
        fab.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP && isRecording) {
                isRecording = videoRecorder.toggleRecordingState()
                Toast.makeText(this, "Saved video to gallery!", Toast.LENGTH_LONG).show()
                true
            } else false
        }
    }

    private fun setUpRecycleView() {
        rvModels.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvModels.adapter = FurnitureAdapter(furnitureModel).apply {
            selectedFurniture.observe(this@MainActivity, Observer {
                this@MainActivity.selectedModel = it
                val newTitle = "Models (${it.title})"
                tvModel.text = newTitle
            })
        }
    }

    private fun setUpDoubleTapArPlanelistener() {
        var firstTapTime = 0L
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->

            if (firstTapTime == 0L) {
                firstTapTime = System.currentTimeMillis()
            } else if (System.currentTimeMillis() - firstTapTime < DOUBLE_TAP_TOLERANCE_MS) {
                firstTapTime = 0L
                loadModel { modelRenderable, viewRenderable ->
                    addNodeToScene(hitResult.createAnchor(), modelRenderable, viewRenderable)
                }
            } else {
                firstTapTime = System.currentTimeMillis()
            }
        }
    }

    private fun rotateViewNodes() {
        for (node in viewNodes) {

            //visible if not null
            node.renderable?.let {
                val camPos = currentScene.camera.worldPosition
                val viewNodePos = node.worldPosition
                val dir = Vector3.subtract(camPos, viewNodePos)
                node.worldRotation = Quaternion.lookRotation(dir, Vector3.up())
            }
        }
    }

    private fun addNodeToScene(
        anchor: Anchor,
        modelRenderable: ModelRenderable,
        viewRenderable: ViewRenderable
    ) {
        val anchorNode = AnchorNode(anchor)
        val modelNode = TransformableNode(arFragment.transformationSystem).apply {
            renderable = modelRenderable
            setParent(anchorNode)
            currentScene.addChild(anchorNode)
            select()
        }

        val viewNode = Node().apply {
            renderable = null
            setParent(anchorNode)
            val box = modelNode.renderable?.collisionShape as Box
            localPosition = Vector3(0f, box.size.y, 0f)
            (viewRenderable.view as Button).setOnClickListener {
                currentScene.removeChild(anchorNode)
                viewNodes.remove(this)
            }
        }

        viewNodes.add(viewNode)
        modelNode.setOnTapListener { _, _ ->

            if (!modelNode.isTransforming) {
                if (viewNode.renderable == null) {
                    viewNode.renderable = viewRenderable
                } else {
                    viewNode.renderable = null
                }
            }
        }

    }

    private fun loadModel(callback: (ModelRenderable, ViewRenderable) -> Unit) {
        val modelRenderable = ModelRenderable.builder()
            .setSource(this, selectedModel.modelResId)
            .build()

        val viewRenderable = ViewRenderable.builder()
            .setView(this, Button(this).apply {
                text = "Delete"
                setBackgroundColor(Color.RED)
                setTextColor(Color.WHITE)
            }).build()

        CompletableFuture.allOf(modelRenderable, viewRenderable).thenAccept {
            callback(modelRenderable.get(), viewRenderable.get())

        }.exceptionally {
            Toast.makeText(this, "Error loading model: $it", Toast.LENGTH_LONG).show()
            null
        }
    }
}
