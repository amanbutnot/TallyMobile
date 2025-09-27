import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch

suspend fun uploadDemoTextToDrive(accessToken: String): String {
    val client = HttpClient()

    val demoContent = "Hello Google Drive 🚀 from Compose Multiplatform"
    val fileName = "demo_upload.txt"
    val boundary = "----WebKitFormBoundary${kotlin.random.Random.nextLong()}"
    // Create multipart body manually
    val multipartBody = buildString {
        // Metadata part
        appendLine("--$boundary")
        appendLine("Content-Type: application/json; charset=UTF-8")
        appendLine()
        appendLine("""{"name": "$fileName"}""")
        appendLine()

        // File content part
        appendLine("--$boundary")
        appendLine("Content-Type: text/plain")
        appendLine()
        append(demoContent)
        appendLine()
        appendLine("--$boundary--")
    }

    val response: HttpResponse = client.post("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart") {
        header(HttpHeaders.Authorization, "Bearer $accessToken")
        header(HttpHeaders.ContentType, "multipart/related; boundary=$boundary")
        setBody(multipartBody)
    }

    return response.bodyAsText()
}

// Alternative approach using resumable upload (recommended for larger files)
suspend fun uploadDemoTextToDriveResumable(accessToken: String): String {
    val client = HttpClient()
    val demoContent = "Hello Google Drive 🚀 from Compose Multiplatform"
    val fileName = "demo_upload.txt"

    // Step 1: Initiate resumable upload
    val initiateResponse: HttpResponse = client.post("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable") {
        header(HttpHeaders.Authorization, "Bearer $accessToken")
        header(HttpHeaders.ContentType, "application/json; charset=UTF-8")
        setBody("""{"name": "$fileName"}""")
    }

    val uploadUrl = initiateResponse.headers["Location"]
        ?: throw Exception("No upload URL received")

    // Step 2: Upload the actual file content
    val uploadResponse: HttpResponse = client.put(uploadUrl) {
        header(HttpHeaders.ContentType, "text/plain")
        setBody(demoContent)
    }

    return uploadResponse.bodyAsText()
}

// Simple approach using media upload
suspend fun uploadDemoTextToDriveSimple(accessToken: String): String {
    val client = HttpClient()
    val demoContent = "Hello Google Drive 🚀 from Compose Multiplatform"
    val fileName = "demo_upload.txt"

    val response: HttpResponse = client.post("https://www.googleapis.com/upload/drive/v3/files?uploadType=media") {
        header(HttpHeaders.Authorization, "Bearer $accessToken")
        header(HttpHeaders.ContentType, "text/plain")
        parameter("name", fileName)
        setBody(demoContent)
    }

    return response.bodyAsText()
}

@Composable
fun DriveUploadScreen() {
    var result by remember { mutableStateOf("Idle") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Button for multipart upload
        Button(onClick = {
            val accessToken = "ya29.a0AS3H6NyDb_K61Gw908Yl0coK1H2yy1W9Ok6vmY4CWlOyOz0-Z05wnuTSGwF0kYGK7mWUXAVM00TGEfzovgWSvUI9EZW7qKrcdD_q6uE-K-bRTfej1GfupW8ZImbGow3jm4Gi8IulLYgLGPLFO9ePscejJvblQIA5NF_LBjcFdC7sudHyFljVgeLI82k3ezQUVOLZoe7PaCgYKAeASARcSFQHGX2MijA34gtVwT7ygXggJQ9NVJA0207" // Replace with your actual token

            scope.launch {
                result = try {
                    uploadDemoTextToDrive(accessToken)
                } catch (e: Exception) {
                    "Multipart Error: ${e.message}"
                }
            }
        }) {
            Text("Upload with Multipart")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Button for resumable upload
        Button(onClick = {
            val accessToken = "ya29.a0AS3H6NyDb_K61Gw908Yl0coK1H2yy1W9Ok6vmY4CWlOyOz0-Z05wnuTSGwF0kYGK7mWUXAVM00TGEfzovgWSvUI9EZW7qKrcdD_q6uE-K-bRTfej1GfupW8ZImbGow3jm4Gi8IulLYgLGPLFO9ePscejJvblQIA5NF_LBjcFdC7sudHyFljVgeLI82k3ezQUVOLZoe7PaCgYKAeASARcSFQHGX2MijA34gtVwT7ygXggJQ9NVJA0207" // Replace with your actual token

            scope.launch {
                result = try {
                    uploadDemoTextToDriveResumable(accessToken)
                } catch (e: Exception) {
                    "Resumable Error: ${e.message}"
                }
            }
        }) {
            Text("Upload with Resumable")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Button for simple media upload
        Button(onClick = {
            val accessToken = "ya29.a0AS3H6NyDb_K61Gw908Yl0coK1H2yy1W9Ok6vmY4CWlOyOz0-Z05wnuTSGwF0kYGK7mWUXAVM00TGEfzovgWSvUI9EZW7qKrcdD_q6uE-K-bRTfej1GfupW8ZImbGow3jm4Gi8IulLYgLGPLFO9ePscejJvblQIA5NF_LBjcFdC7sudHyFljVgeLI82k3ezQUVOLZoe7PaCgYKAeASARcSFQHGX2MijA34gtVwT7ygXggJQ9NVJA0207"

            scope.launch {
                result = try {
                    uploadDemoTextToDriveSimple(accessToken)
                } catch (e: Exception) {
                    "Simple Error: ${e.message}"
                }
            }
        }) {
            Text("Upload Simple Media")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(result)
    }
}