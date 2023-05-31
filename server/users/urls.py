from django.urls import path
from . import views

urlpatterns = [
    path('register/', views.register),
    path('authorize/', views.credentials_authorize),
    path('get_profile_info/<str:nickname>', views.get_profile_info),
    path('change_profile/<str:idToken>', views.change_profile),
    path('plus_reputation/', views.plus_reputation),
    path('minus_reputation/', views.minus_reputation),
]
