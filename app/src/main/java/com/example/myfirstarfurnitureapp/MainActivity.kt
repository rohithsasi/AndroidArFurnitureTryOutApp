package com.example.myfirstarfurnitureapp

import android.graphics.Color
import android.os.Bundle
import android.os.ProxyFileDescriptorCallback
import android.util.TypedValue
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
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var selectedModel: FurnitureImage
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
        setUpBottomSheet()
        setUpRecycleView()
        setUpDoubleTapArPlanelistener()
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

    private  fun setUpDoubleTapArPlanelistener(){
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->

            loadModel { modelRenderable, viewRenderable ->
                addNodeToScene(hitResult.createAnchor(),modelRenderable,viewRenderable)
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
            localPosition = Vector3(0f,box.size.y, 0f)
            (viewRenderable.view as Button).setOnClickListener {
                currentScene.removeChild(anchorNode)
            }
        }

        modelNode.setOnTapListener { _, _ ->

            if(!modelNode.isTransforming){
                if(viewNode.renderable == null) {
                    viewNode.renderable = viewRenderable
                } else {
                    viewNode.renderable = null
                }
            }
        }

    }

    private fun loadModel(callback: (ModelRenderable,ViewRenderable) ->Unit){
        val modelRenderable = ModelRenderable.builder()
            .setSource(this, selectedModel.modelResId)
            .build()

        val viewRenderable = ViewRenderable.builder()
            .setView(this, Button(this).apply {
                text= "Delete"
                setBackgroundColor(Color.RED)
                setTextColor(Color.WHITE)
            }).build()

        CompletableFuture.allOf(modelRenderable,viewRenderable).thenAccept{
            callback(modelRenderable.get(), viewRenderable.get())

        }.exceptionally {
            Toast.makeText(this, "Error loading model: $it", Toast.LENGTH_LONG).show()
            null
        }
    }

}
