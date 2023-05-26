from django.urls import path
from . import views

urlpatterns = [
    path('create_session/', views.create_session),
    path('get_session_info/', views.get_session_info),
    path('delete_session/', views.delete_session),
]
