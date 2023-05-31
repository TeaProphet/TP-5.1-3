from django.urls import path
from . import views

urlpatterns = [
    path('create_session/', views.create_session),
    path('get_session_info/<int:session_id>', views.get_session_info),
    path('delete_session/<int:session_id>', views.delete_session),
    path('get_sessions/', views.get_sessions)
]
