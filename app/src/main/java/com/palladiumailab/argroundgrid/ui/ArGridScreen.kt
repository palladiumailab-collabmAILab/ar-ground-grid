package com.palladiumailab.argroundgrid.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.palladiumailab.argroundgrid.grid.GridGeometry
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.LineNode
import io.github.sceneview.rememberOnGestureListener
import java.util.concurrent.atomic.AtomicReference

@Composable
fun ArGridScreen() {
    val latestFrame = remember { AtomicReference<Frame?>(null) }
    val gridLines = remember { GridGeometry.generate() }
    var anchor by remember { mutableStateOf<Anchor?>(null) }
    var trackingLabel by remember { mutableStateOf("床を探しています") }

    Box(modifier = Modifier.fillMaxSize()) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            planeRenderer = anchor == null,
            sessionConfiguration = { _, config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            },
            onSessionUpdated = { _, frame ->
                latestFrame.set(frame)
                val nextLabel = when (frame.camera.trackingState) {
                    TrackingState.TRACKING -> if (anchor == null) {
                        "床をタップしてグリッドを配置"
                    } else {
                        "配置済み • 10 cm grid / 1 m major"
                    }
                    TrackingState.PAUSED -> "追跡待機中: ${frame.camera.trackingFailureReason}"
                    TrackingState.STOPPED -> "AR追跡停止"
                }
                if (nextLabel != trackingLabel) trackingLabel = nextLabel
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { event, node ->
                    if (anchor != null || node != null) return@rememberOnGestureListener
                    val frame = latestFrame.get() ?: return@rememberOnGestureListener
                    val hit = frame.hitTest(event).firstOrNull { result ->
                        val trackable = result.trackable
                        trackable is Plane &&
                            trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                            trackable.isPoseInPolygon(result.hitPose)
                    }
                    hit?.createAnchor()?.let { anchor = it }
                },
            ),
        ) {
            val minorMaterial = remember(materialLoader) {
                materialLoader.createColorInstance(Color(0.15f, 0.85f, 1f, 0.72f), unlit = true)
            }
            val majorMaterial = remember(materialLoader) {
                materialLoader.createColorInstance(Color(1f, 0.78f, 0.12f, 0.95f), unlit = true)
            }

            anchor?.let { placedAnchor ->
                AnchorNode(anchor = placedAnchor) {
                    gridLines.forEach { line ->
                        LineNode(
                            start = Position(line.startX, 0.002f, line.startZ),
                            end = Position(line.endX, 0.002f, line.endZ),
                            materialInstance = if (line.isMajor) majorMaterial else minorMaterial,
                        )
                    }
                }
            }
        }

        Text(
            text = trackingLabel,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.62f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (anchor != null) {
                Button(
                    onClick = {
                        anchor?.detach()
                        anchor = null
                        trackingLabel = "床を探しています"
                    },
                ) {
                    Text("リセットして再配置")
                }
            }
        }
    }
}
