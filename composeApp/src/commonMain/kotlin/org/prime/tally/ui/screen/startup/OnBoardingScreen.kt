import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen

object OnBoardingScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val colors = MaterialTheme.colorScheme
        val type = MaterialTheme.typography


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            contentAlignment = Alignment.Center
        ) {
            val items = listOf("Welcome", "Manage Finances", "Get Started")

            val carouselState = rememberCarouselState(itemCount = { items.size })
            HorizontalMultiBrowseCarousel(
                modifier = Modifier.fillMaxWidth(),
                state = carouselState,
                itemSpacing = 16.dp,
                preferredItemWidth = 250.dp
            ) { itemIndex ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
                         .maskClip(MaterialTheme.shapes.extraLarge)
                        .background(colors.primary)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[itemIndex],
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = colors.onPrimary
                    )
                }
            }

        }
    }
}
