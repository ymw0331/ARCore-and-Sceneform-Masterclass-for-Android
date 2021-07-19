package com.wayneyong.snapchat_filter

import android.os.Bundle
import android.view.View
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.util.*

class FaceArFragment : ArFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        planeDiscoveryController.hide() //hide the white dots
        planeDiscoveryController.setInstructionView(null) //hide the white hand
    }

    override fun getSessionFeatures(): MutableSet<Session.Feature> {
        return EnumSet.of(Session.Feature.FRONT_CAMERA)
    }

    override fun getSessionConfiguration(session: Session?): Config {
        val config = Config(session)
        config.augmentedFaceMode = Config.AugmentedFaceMode.MESH3D
        return config
    }
}