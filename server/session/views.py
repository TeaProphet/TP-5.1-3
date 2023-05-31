import json
from django.http import JsonResponse
from drf_spectacular.utils import extend_schema
from python_jwt import _JWTError
from rest_framework.decorators import api_view
from rest_framework.exceptions import ValidationError
from rest_framework.response import Response
from onboardProject import settings
from session import models


@extend_schema(
    request=models.SessionSerializer,
    tags=['sessions']
)
@api_view(['POST'])
def create_session(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        session_id = 0
        try:
            all_sessions = settings.database.child(settings.SESSIONS_TABLE).get().each()
            session_id = len(all_sessions)
        except TypeError:
            pass
        body_data['session_id'] = session_id
        session = models.SessionSerializer(data=dict(body_data))
        if not session.is_valid():
            raise ValidationError
        session = session.save()
        auth_user = settings.auth.verify_id_token(body_data['idToken'])
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'})
    except _JWTError as token_error:
        if token_error.args[0] == 'invalid JWT format':
            return JsonResponse({'error': 'INVALID_TOKEN'})
        else:
            return JsonResponse({'error': 'EXPIRED_TOKEN'})
    uid = auth_user['user_id']
    nickname = settings.database.child(settings.USERS_TABLE).child(uid).child('nickname').get().val()
    session.save_session(nickname, uid)

    return Response(status=204)


@extend_schema(
    request=models.SessionDeleteSerializer,
    tags=['Sessions']
)
@api_view(['DELETE'])
def delete_session(request, session_id):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        id_token = body_data.get('idToken')
    except ValidationError:
        return JsonResponse({'error': 'INVALID_DATA'})
    if not settings.database.child('session').child(session_id).get().val():
        return JsonResponse({'error': 'INVALID_SESSION_ID'})
    requester_uid = settings.auth.verify_id_token(id_token).get('localId')
    requester_nickname = settings.database.child(settings.USERS_TABLE).child(requester_uid).child('nickname').get().val()
    owner_nickname = settings.database.child('session').child(session_id).child('owner').get().val()
    if owner_nickname == requester_nickname:
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
    return JsonResponse(models.SessionSerializer(session).data)

@api_view(['POST'])
def get_sessions(request):
    raw_sessions_info = settings.database.child(settings.SESSIONS_TABLE).get().val()
    sessions_info = {}
    for i in range(len(raw_sessions_info)):
        sessions_info[i] = raw_sessions_info[i]
    return JsonResponse(sessions_info)
