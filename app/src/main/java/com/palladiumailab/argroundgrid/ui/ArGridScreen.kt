package com.palladiumailab.argroundgrid.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.palladiumailab.argroundgrid.grid.GridGeometry
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import io.github.sceneview.node.CubeNode
import io.github.sceneview.rememberOnGestureListener
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

@Composable
fun ArGridScreen() {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasCameraPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(24.dp),
            ) {
                Text("AR表示にはカメラ権限が必要です")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("カメラを許可")
                }
            }
        }
        return
    }

    ArGridContent()
}

@Composable
private fun ArGridContent() {
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
            onSessionFailed = { exception ->
                latestFrame.set(null)
                trackingLabel = "ARを開始できません: ${exception.javaClass.simpleName}"
            },
            onSessionUpdated = { session, frame ->
                latestFrame.set(frame)
                val nextLabel = when (frame.camera.trackingState) {
                    TrackingState.TRACKING -> if (anchor != null) {
                        "配置済み • 10 cm grid / 1 m major"
                    } else {
                        val hasHorizontalPlane = session
                            .getAllTrackables(Plane::class.java)
                            .any { plane ->
                                plane.trackingState == TrackingState.TRACKING &&
                                    plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                            }
                        if (hasHorizontalPlane) {
                            "床をタップしてグリッドを配置"
                        } else {
                            "床を探しています"
                        }
                    }
                    TrackingState.PAUSED -> "追跡待機中: ${frame.camera.trackingFailureReason}"
                    TrackingState.STOPPED -> "AR追跡停止"
                }
                if (nextLabel != trackingLabel) trackingLabel = nextLabel
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { event, _ ->
                    if (anchor != null) return@rememberOnGestureListener
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
                materialLoader.createUnlitColorInstance(Color(0.15f, 0.85f, 1f, 0.78f))
            }
            val majorMaterial = remember(materialLoader) {
                materialLoader.createUnlitColorInstance(Color(1f, 0.78f, 0.12f, 0.98f))
            }

            anchor?.let { placedAnchor ->
                AnchorNode(anchor = placedAnchor) {
                    gridLines.forEach { line ->
                        val lineWidth = if (line.isMajor) 0.012f else 0.006f
                        val lengthX = abs(line.endX - line.startX)
                        val lengthZ = abs(line.endZ - line.startZ)
                        val centerX = (line.startX + line.endX) / 2f
                        val centerZ = (line.startZ + line.endZ) / 2f

                        CubeNode(
                            size = if (lengthX > 0f) {
                                Size(lengthX, 0.004f, lineWidth)
                            } else {
                                Size(lineWidth, 0.004f, lengthZ)
                            },
                            position = Position(centerX, 0.004f, centerZ),
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
