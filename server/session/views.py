import json
from django.http import JsonResponse
from rest_framework.decorators import api_view
from rest_framework.exceptions import ValidationError
from rest_framework.response import Response
from onboardProject import settings
from session import models
from firebase_tools.users_tools import get_user, get_user_token


@api_view(['POST'])
def create_session(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        session_id = 0
        try:
            all_sessions = settings.database.child("sessions").get().each()
            session_id = len(all_sessions)
        except TypeError:
            pass
        body_data['session_id'] = session_id
        session = models.SessionSerializer(data=dict(body_data))
        if not session.is_valid():
            raise ValidationError
        session = session.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'})
    login = get_user_token(body_data['idToken']).key()
    session.save_session(login)

    return Response(status=204)


@api_view(['POST'])
def delete_session(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        id_token = body_data.get('idToken')
        session_id = body_data.get('sessionId')
    except ValidationError:
        return JsonResponse({'error': 'INVALID_DATA'})
    if not settings.database.child('session').child(session_id).get().val():
        return JsonResponse({'error': 'INVALID_SESSION_ID'})
    requester_login = get_user_token(id_token).key()
    owner_login = settings.database.child('session').child(session_id).child('owner').get().val()
    if owner_login == requester_login:
        sessions_list = settings.database.child('session').get().val()
        sessions_list.pop(session_id)
        settings.database.child('session').set(sessions_list)
    else:
        return JsonResponse({'error': 'PERMISSION_DENIED'})
    return Response(status=204)

@api_view(['POST'])
def get_session_info(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        session_id = body_data.get('sessionId')
        session_info = settings.database.child('sessions').child(session_id).get().val()
        if not session_info:
            raise ValidationError
        else:
            session_info = dict(session_info)
        session_info['session_id'] = session_id
        session = models.SessionSerializer(data=session_info)
        if not session.is_valid():
            raise ValidationError
        session = session.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_SESSION_ID'})
    session.add_reputation_to_players()
    return JsonResponse(models.SessionSerializer(session).data)
