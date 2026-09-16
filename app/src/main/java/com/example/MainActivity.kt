package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AppDatabase
import com.example.data.ProjectRepository
import com.example.ui.editor.EditorScreen
import com.example.ui.editor.EditorViewModel
import com.example.ui.projects.ProjectsScreen
import com.example.ui.projects.ProjectsViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = ProjectRepository(database.projectDao())

        setContent {
            MyApplicationTheme {
                VNStudioApp(repository = repository)
            }
        }
    }
}

@Composable
fun VNStudioApp(repository: ProjectRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "projects",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("projects") {
            val projectsViewModel: ProjectsViewModel = viewModel(
                factory = ProjectsViewModel.Factory(repository)
            )
            ProjectsScreen(
                viewModel = projectsViewModel,
                onOpenProject = { projectId ->
                    navController.navigate("editor/$projectId")
                }
            )
        }

        composable(
            route = "editor/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val editorViewModel: EditorViewModel = viewModel(
                key = "editor_$projectId",
                factory = EditorViewModel.Factory(repository, projectId)
            )

            EditorScreen(
                viewModel = editorViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
