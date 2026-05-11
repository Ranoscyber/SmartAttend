package com.example.smartattend.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.smartattend.ui.admin.AddHrScreen
import com.example.smartattend.ui.admin.AdminRootScreen
import com.example.smartattend.ui.auth.ForgotPasswordScreen
import com.example.smartattend.ui.auth.LoginScreen
import com.example.smartattend.ui.auth.SplashScreen
import com.example.smartattend.ui.employee.EmployeeRootScreen
import com.example.smartattend.ui.hr.HrRootScreen
import com.example.smartattend.viewmodel.AdminViewModel
import com.example.smartattend.viewmodel.AttendanceViewModel
import com.example.smartattend.viewmodel.AuthViewModel
import com.example.smartattend.viewmodel.EmployeeViewModel
import com.example.smartattend.viewmodel.HrViewModel
import com.example.smartattend.viewmodel.AppSettingsViewModel

@Composable
fun AppNavGraph(
    appSettingsViewModel: AppSettingsViewModel
) {
    val navController: NavHostController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel()
    val adminViewModel: AdminViewModel = viewModel()
    val hrViewModel: HrViewModel = viewModel()
    val employeeViewModel: EmployeeViewModel = viewModel()
    val attendanceViewModel: AttendanceViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen()

            androidx.compose.runtime.LaunchedEffect(Unit) {
                authViewModel.checkCurrentSession(
                    onAdmin = {
                        navController.navigate(Routes.ADMIN_DASHBOARD) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onHr = {
                        navController.navigate(Routes.HR_DASHBOARD) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onEmployee = {
                        navController.navigate(Routes.EMPLOYEE_HOME) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNotLoggedIn = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onForgotPassword = {
                    authViewModel.resetAuthStateForForgotPassword()

                    navController.navigate(Routes.FORGOT_PASSWORD) {
                        launchSingleTop = true
                    }
                },
                onAdminLogin = {
                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onHrLogin = {
                    navController.navigate(Routes.HR_DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onEmployeeLogin = {
                    navController.navigate(Routes.EMPLOYEE_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onBack = {
                    authViewModel.clearResetEmailSent()
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ADMIN_DASHBOARD) {
            AdminRootScreen(
                viewModel = adminViewModel,
                appSettingsViewModel = appSettingsViewModel,
                onAddHrClick = {
                    navController.navigate(Routes.ADD_HR)
                },
                onLogoutClick = {
                    authViewModel.logout()

                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADD_HR) {
            AddHrScreen(
                viewModel = adminViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onHrCreated = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HR_DASHBOARD) {
            HrRootScreen(
                viewModel = hrViewModel,
                appSettingsViewModel = appSettingsViewModel,
                onLogoutClick = {
                    authViewModel.logout()

                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HR_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.EMPLOYEE_HOME) {
            EmployeeRootScreen(
                employeeViewModel = employeeViewModel,
                attendanceViewModel = attendanceViewModel,
                appSettingsViewModel = appSettingsViewModel,
                onLogoutClick = {
                    authViewModel.logout()

                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.EMPLOYEE_HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}