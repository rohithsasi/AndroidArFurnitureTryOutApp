package com.example.myfirstarfurnitureapp

import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.schemas.lull.ModelPipelineRenderableDef

object SceneUtil {


    fun addNodeToScene(anchor: Anchor, modelRenderable: ModelRenderable, viewRenderable : ViewRenderable){

        val anchorNode = AnchorNode(anchor)
        //val modelNode = TransformableNode

    }
}