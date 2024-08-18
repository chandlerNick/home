package com.example.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gallery.ui.theme.ArtSpaceTheme
import first.project.gallery.R

// Now it's version 2
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArtSpaceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    ) {
                    ArtSpaceLayout()
                }
            }
        }
    }
}



@Composable
fun ArtSpaceLayout() {
    var pageNumber by remember { mutableStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .background(color = Color.LightGray)
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        when(pageNumber) {
            0 -> {
                // Want on clickPrev to set pageNumber to 3 (last state)
                ArtImages(
                    mainImage = R.drawable.adams,
                    imageTitle = R.string.adams
                )
                ArtDescription(
                    imageTitle = R.string.adams,
                    imageDescription = R.string.adams_desc
                )
                NavButtons(
                    clickPrev = {pageNumber = 3 },
                    clickNext = {pageNumber = 1 },
                    )
            }
            1 -> {
                ArtImages(
                    mainImage = R.drawable.rainier,
                    imageTitle = R.string.rainier
                )
                ArtDescription(
                    imageTitle = R.string.rainier,
                    imageDescription = R.string.rainier_desc
                )
                NavButtons(
                    clickPrev = {pageNumber = 0 },
                    clickNext = {pageNumber = 2 },
                )
            }
            2 -> {
                ArtImages(
                    mainImage = R.drawable.granite,
                    imageTitle = R.string.granite
                )
                ArtDescription(
                    imageTitle = R.string.granite,
                    imageDescription = R.string.granite_desc
                )
                NavButtons(
                    clickPrev = {pageNumber = 1 },
                    clickNext = {pageNumber = 3 },
                )
            }
            else -> {
                // Want clickNext to set pageNumber to 0
                ArtImages(
                    mainImage = R.drawable.st_helens,
                    imageTitle = R.string.st_helens
                )
                ArtDescription(
                    imageTitle = R.string.st_helens,
                    imageDescription = R.string.st_helens_desc
                )
                NavButtons(
                    clickPrev = {pageNumber = 2 },
                    clickNext = {pageNumber = 0 },
                )
            }
        }

    }
}

@Composable
fun ArtImages(
    @DrawableRes mainImage: Int,
    @StringRes imageTitle: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .background(color = colorResource(id = R.color.borders), shape = RectangleShape)
            .padding(8.dp)

    ) {
        Image(
            painter = painterResource(mainImage),
            contentDescription = stringResource(id = imageTitle)
        )
    }

}


@Composable
fun ArtDescription(
    @StringRes imageTitle: Int,
    @StringRes imageDescription: Int,
    modifier: Modifier = Modifier
) {
    Column (
        modifier = modifier
            .background(color = colorResource(id = R.color.borders), shape = RoundedCornerShape(10.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(id = imageTitle),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(12.dp)
        )
        Text(
            stringResource(id = imageDescription),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(12.dp)
        )
    }
}

@Composable
fun NavButtons(
    clickPrev: () -> Unit,
    clickNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .padding(12.dp)
    ) {
        // Previous Button
        Button(
            onClick = clickPrev,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.borders),
                contentColor = Color.White)
        ) {
            Text(text = stringResource(id = R.string.prev))
        }
        Spacer(modifier = Modifier.width(32.dp))
        // Next Button
        Button(
            onClick = clickNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.borders),
                contentColor = Color.White)
        ) {
            Text(text = stringResource(id = R.string.next))
        }
    }

}





@Preview(showBackground = true)
@Composable
fun ArtSpaceLayoutPreview() {
    ArtSpaceTheme {
        ArtSpaceLayout()
    }
}
