import json
from django.http import JsonResponse, HttpRequest
from drf_spectacular.types import OpenApiTypes
from drf_spectacular.utils import extend_schema, OpenApiParameter
from python_jwt import _JWTError
from rest_framework.decorators import api_view
from rest_framework.exceptions import ValidationError
from rest_framework.response import Response
from onboardProject import settings
from session import models
from utils.serializing import complete_serialize


@extend_schema(
    request=models.SessionRegistrationSerializer,
    responses=None,
    tags=['Sessions']
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
        session = models.SessionRegistrationSerializer(data=dict(body_data))
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
    session.register_session(nickname, uid)

    return Response(status=204)


@extend_schema(
    request=None,
    responses=None,
    tags=['Sessions'],
    parameters=[OpenApiParameter("idToken", OpenApiTypes.STR, OpenApiParameter.QUERY, required=True)],
)
@api_view(['DELETE'])
def delete_session(request, session_id):
    try:
        id_token = request.GET.get('idToken')
    except ValidationError:
        return JsonResponse({'error': 'INVALID_DATA'})
    if not settings.database.child(settings.SESSIONS_TABLE).child(session_id).get().val():
        return JsonResponse({'error': 'INVALID_SESSION_ID'})
    requester_uid = settings.auth.verify_id_token(id_token)['user_id']
    is_admin = settings.database.child(settings.USERS_TABLE).child(requester_uid).child('user_data').child('is_admin').get().val()
    requester_nickname = settings.database.child(settings.USERS_TABLE).child(requester_uid).child('nickname').get().val()
    owner_nickname = settings.database.child(settings.SESSIONS_TABLE).child(session_id).child('owner').get().val()
    if owner_nickname == requester_nickname or is_admin:
        delete_session_algorythm(requester_uid, session_id)
    else:
        return JsonResponse({'error': 'PERMISSION_DENIED'})
    return Response(status=204)


def delete_session_algorythm(requester_uid, session_id):
    session_info = settings.database.child(settings.SESSIONS_TABLE).child(session_id).get().val()
    settings.database.child(settings.SESSIONS_TABLE).child(session_id).remove()
    players_list = session_info.get('players')
    for nickname in players_list:
        player_uid = settings.database.child(settings.USERS_TABLE).order_by_child('nickname').equal_to(nickname).get().val().popitem()[0]
        played_sessions = settings.database.child(settings.USERS_TABLE).child(player_uid).child('user_data').child('played_sessions').get().val()
        if played_sessions.__contains__(session_id):
            id_in_played = played_sessions.index(session_id)
            played_sessions.pop(id_in_played)
            settings.database.child(settings.USERS_TABLE).child(player_uid) \
                .child('user_data').child('played_sessions').set(played_sessions)


@extend_schema(
    responses=models.SessionPublicInfoSerializer,
    request=None,
    tags=['Sessions']
)
@api_view(['GET'])
def get_session_info(request, session_id):
    try:
        session_info = settings.database.child(settings.SESSIONS_TABLE).child(session_id).get().val()
        if not session_info:
            raise ValidationError
        else:
            session_info = dict(session_info)
        session_info['session_id'] = session_id
        session = models.SessionPublicInfoSerializer(data=session_info)
        if not session.is_valid():
            raise ValidationError
        session = session.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_SESSION_ID'})
    return JsonResponse(models.SessionPublicInfoSerializer(session).data)


@extend_schema(
    responses=models.SessionsListSerializer,
    request=None,
    tags=['Sessions']
)
@api_view(['GET'])
def get_sessions(request):
    raw_sessions_info = settings.database.child(settings.SESSIONS_TABLE).get().val()
    if raw_sessions_info:
        sessions_info = []
        for i in range(len(raw_sessions_info)):
            if raw_sessions_info[i]:
                serialized_raw = models.SessionPublicShortInfoSerializer(data=raw_sessions_info[i])
                serialized_raw.is_valid()
                serialized = serialized_raw.validated_data
                serialized['sessionId'] = i
                sessions_info.append(serialized)
    else:
        sessions_info = []
    return JsonResponse(sessions_info, safe=False)


@extend_schema(
    request=None,
    responses=None,
    tags=['Sessions'],
    parameters=[OpenApiParameter("idToken", OpenApiTypes.STR, OpenApiParameter.QUERY, required=True),
                OpenApiParameter("session_id", OpenApiTypes.INT, OpenApiParameter.PATH, required=True)]
)
@api_view(['POST'])
def join_session(request, session_id):
    try:
        id_token = request.GET.get('idToken')
        decoded_token = settings.auth.verify_id_token(id_token)
        requester_uid = decoded_token['user_id']
        session_info = settings.database.child(settings.SESSIONS_TABLE).child(session_id).get().val()
        if not session_info:
            raise ValidationError
    except _JWTError as token_error:
        if token_error.args[0] == 'invalid JWT format':
            return JsonResponse({'error': 'INVALID_TOKEN'})
        else:
            return JsonResponse({'error': 'EXPIRED_TOKEN'})
    except ValidationError:
        return JsonResponse({'error': 'INVALID_DATA'})

    played_sessions = settings.database.child(settings.USERS_TABLE).child(requester_uid).child('user_data').child('played_sessions').get().val()
    if played_sessions and played_sessions.__contains__(session_id):
        return JsonResponse({'error': 'ALREADY_JOINED'})
    elif played_sessions:
        played_sessions.append(session_id)
    else:
        played_sessions = [session_id]
    session_info = dict(session_info)
    session_players = session_info.get('players')
    if session_info.get('players_max') == len(session_players):
        return JsonResponse({'error': 'ALREADY_FULL'})
    settings.database.child(settings.USERS_TABLE).child(requester_uid).child('user_data').child('played_sessions').set(played_sessions)
    nickname = settings.database.child(settings.USERS_TABLE).child(requester_uid).child('nickname').get().val()
    session_players.append(nickname)
    settings.database.child(settings.SESSIONS_TABLE).child(session_id).set(session_info)
    return Response(status=204)


@extend_schema(
    request=None,
    responses=None,
    tags=['Sessions'],
    parameters=[OpenApiParameter("idToken", OpenApiTypes.STR, OpenApiParameter.QUERY, required=True),
                OpenApiParameter("session_id", OpenApiTypes.INT, OpenApiParameter.PATH, required=True)]
)
@api_view(['POST'])
def leave_session(request, session_id):
    try:
        id_token = request.GET.get('idToken')
        decoded_token = settings.auth.verify_id_token(id_token)
        requester_uid = decoded_token['user_id']
        session_info = settings.database.child(settings.SESSIONS_TABLE).child(session_id).get().val()
        if not session_info:
            raise ValidationError
    except _JWTError as token_error:
        if token_error.args[0] == 'invalid JWT format':
            return JsonResponse({'error': 'INVALID_TOKEN'})
        else:
            return JsonResponse({'error': 'EXPIRED_TOKEN'})
    except ValidationError:
        return JsonResponse({'error': 'INVALID_DATA'})

    session_info = dict(session_info)
    session_players = session_info.get('players')
    nickname = settings.database.child(settings.USERS_TABLE).child(requester_uid).child('nickname').get().val()
    if session_info.get('owner') == nickname:
        delete_session_algorythm(requester_uid, session_id)
    else:
        played_sessions = settings.database.child(settings.USERS_TABLE).child(requester_uid).child('user_data').child('played_sessions').get().val()
        if not played_sessions or not played_sessions.__contains__(session_id):
            return JsonResponse({'error': 'INVALID_DATA'})
        played_sessions.remove(session_id)
        settings.database.child(settings.USERS_TABLE).child(requester_uid).child('user_data').child('played_sessions').set(played_sessions)
        session_players.remove(nickname)
        settings.database.child(settings.SESSIONS_TABLE).child(session_id).set(session_info)
    return Response(status=204)


@extend_schema(
    request=None,
    responses=None,
    tags=['Sessions'],
    parameters=[OpenApiParameter("idToken", OpenApiTypes.STR, OpenApiParameter.QUERY, required=True),
                OpenApiParameter("session_id", OpenApiTypes.INT, OpenApiParameter.PATH, required=True),
                OpenApiParameter("new_name", OpenApiTypes.STR, OpenApiParameter.PATH, required=True)]
)
@api_view(['PUT'])
def change_name(request, session_id, new_name):
    try:
        id_token = request.GET.get('idToken')
        decoded_token = settings.auth.verify_id_token(id_token)
        requester_uid = decoded_token['user_id']
        session_info = settings.database.child(settings.SESSIONS_TABLE).child(session_id).get().val()
        if not session_info:
            raise ValidationError
    except _JWTError as token_error:
        if token_error.args[0] == 'invalid JWT format':
            return JsonResponse({'error': 'INVALID_TOKEN'})
        else:
            return JsonResponse({'error': 'EXPIRED_TOKEN'})
    except ValidationError:
        return JsonResponse({'error': 'INVALID_DATA'})
    is_admin = settings.database.child(settings.USERS_TABLE).child(requester_uid).child('user_data').child(
        'is_admin').get().val()
    requester_nickname = settings.database.child(settings.USERS_TABLE).child(requester_uid).child(
        'nickname').get().val()
    owner_nickname = settings.database.child(settings.SESSIONS_TABLE).child(session_id).child('owner').get().val()
    if owner_nickname == requester_nickname or is_admin:
        settings.database.child(settings.SESSIONS_TABLE).child(session_id).child('name').set(new_name)
    else:
        return JsonResponse({'error': 'PERMISSION_DENIED'})
    return Response(status=204)

