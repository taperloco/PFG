package com.example.recado.ui

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recado.R

@Preview(showBackground = true)
@Composable
fun Welcome(viewModel: ViewModel = viewModel(),
    onLoginButtonClicked: () -> Unit = {},
                onRegisterButtonClicked: () -> Unit = {}) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val activity = (LocalContext.current as? Activity)

    Box(modifier = Modifier.size(width = screenWidthDp.dp, height = screenHeightDp.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .size((screenHeightDp * 0.4f).dp),
                painter = painterResource(id = R.drawable.recado_logo),
                contentDescription = "Recado Logo",
            )

            ElevatedButton(
                onClick = onRegisterButtonClicked,
                modifier = Modifier
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Registrar usuario")
            }

            Button(
                onClick = onLoginButtonClicked,
                modifier = Modifier
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Iniciar sesión")
            }

            Button(
                onClick = {viewModel.logout()
                    activity?.finish()},
                modifier = Modifier
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Cerrar sesión")
            }

            Button(
                onClick = {activity?.finish()},
                modifier = Modifier
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Salir")
            }

        }
    }
}

