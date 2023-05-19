from django.urls import path
from . import views

urlpatterns = [
    path('register/', views.register),
    path('authorize/', views.credentials_authorize),
    path('token_authorize/', views.token_authorize),
    path('create_session/', views.create_session),
    path('get_profile_info/', views.get_profile_info),
    path('get_session_info/', views.get_session_info)
]
