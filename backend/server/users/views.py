import json
import math
from django.http import JsonResponse
from drf_spectacular.utils import extend_schema
from python_jwt import _JWTError
from requests import HTTPError
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.exceptions import ValidationError
from rest_framework.response import Response
from onboardProject import settings
from users import models, serializers
from utils.errors import NotPlayedSameError
from utils.serializing import complete_serialize


@extend_schema(
    request=serializers.RegistrationSerializer,
    responses={status.HTTP_200_OK: serializers.RegistrationSerializer,
               status.HTTP_400_BAD_REQUEST: serializers.RegistrationSerializer},
    description="Метод для регистрации пользователя.",
    tags=["Users"]
)
@api_view(['POST'])
def register(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        if not body_data.keys().__contains__('password'):
            raise ValidationError
        password = body_data['password']
        credentials = complete_serialize(body_data, serializers.RegistrationSerializer)
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'}, status=400)
    try:
        auth_user = settings.auth.create_user_with_email_and_password(credentials.login, password)
    except HTTPError as exception:
        return JsonResponse({'error': extract_http_error_message(exception.args[1])})
    user = models.User(uid=auth_user.get('localId'), login=credentials.login, user_data=models.UserData(),
                       nickname=credentials.nickname)
    try:
        user_data = dict(serializers.UserDataSerializer(user.user_data).data)
        if len(settings.database.child(settings.USERS_TABLE).order_by_child('nickname').equal_to(
                user.nickname).get().each()) > 0:
            raise ValueError('NICKNAME_EXISTS')
        settings.database.child(settings.USERS_TABLE).child(user.uid).child('user_data').update(user_data)
        settings.database.child(settings.USERS_TABLE).child(user.uid).child('login').set(user.login)
        settings.database.child(settings.USERS_TABLE).child(user.uid).child('nickname').set(user.nickname)
    except ValueError as e:
        settings.auth.delete_user_account(auth_user['idToken'])
        return JsonResponse({'error': e.args[0]})
    return JsonResponse({'idToken': auth_user.get('idToken')})


@extend_schema(
    request=serializers.AuthorizationSerializer,
    responses={status.HTTP_200_OK: serializers.AuthorizationSerializer,
               status.HTTP_400_BAD_REQUEST:serializers.AuthorizationSerializer},
    description="Метод для авторизации пользователя.",
    tags=["Users"]
)
@api_view(['POST'])
def credentials_authorize(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        auth_user = settings.auth.sign_in_with_email_and_password(body_data['login'], body_data['password'])
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'}, status=400)
    except HTTPError as exception:
        return JsonResponse({'error': extract_http_error_message(exception.args[1])})
    nickname = settings.database.child(settings.USERS_TABLE).child(auth_user.get('localId')).child('nickname').get().val()
    return JsonResponse({'idToken': auth_user.get('idToken'), 'nickname': nickname})


@extend_schema(
    request=serializers.SearchedNicknameSerializer,
    description='Метод, возвращающий публичную информацию о пользователе по его никнейму. '
                'Предоставляется никнейм пользователя.',
    responses={status.HTTP_200_OK: serializers.SearchedNicknameSerializer,
               status.HTTP_400_BAD_REQUEST: serializers.SearchedNicknameSerializer},
    tags=["Users"]
)
@api_view(['GET'])
def get_profile_info(request, nickname):
    user = settings.database.child(settings.USERS_TABLE).order_by_child('nickname').equal_to(nickname).get()
    if len(user.each()) == 0:
        return JsonResponse({'error': 'INVALID_DATA'}, status=400)
    user_data = user.val().popitem()[1].get('user_data')
    serialized_data = serializers.UserDataSerializer(data=user_data)
    serialized_data.is_valid()
    return JsonResponse(serialized_data.data)


@extend_schema(
    request=serializers.ChangingUserDataSerializer,
    responses={status.HTTP_204_NO_CONTENT: None,
               status.HTTP_400_BAD_REQUEST: serializers.ChangingUserDataSerializer},
    description="Метод для смены публичной информации о пользователе. Предоставляются:\n"
                "1. idToken - токен пользователя.",
    tags=["Users"]
)
@api_view(['PUT'])
def change_profile(request, idToken):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        user_data = serializers.ChangingUserDataSerializer(data=body_data)
        if not user_data.is_valid():
            raise ValidationError
        user_data = user_data.save()
        decoded_token = settings.auth.verify_id_token(idToken)
        uid = decoded_token['user_id']
    except ValidationError:
        return JsonResponse({'error': 'INVALID_DATA'}, status=400)
    except _JWTError as token_error:
        if token_error.args[0] == 'invalid JWT format':
            return JsonResponse({'error': 'INVALID_TOKEN'}, status=400)
        else:
            return JsonResponse({'error': 'EXPIRED_TOKEN'}, status=400)
    settings.database.child(settings.USERS_TABLE).child(uid).child('user_data').update(dict(serializers.ChangingUserDataSerializer(user_data).data))
    return Response(status=204)


@extend_schema(
    request=serializers.PlusReputationRequestSerializer,
    responses={status.HTTP_200_OK: serializers.PlusReputationRequestSerializer,
               status.HTTP_400_BAD_REQUEST: serializers.PlusReputationRequestSerializer,
               status.HTTP_403_FORBIDDEN: serializers.PlusReputationRequestSerializer},
    description="Метод для увеличения репутации пользователя другим пользователем.",
    tags=["Users"],
)
@api_view(['POST'])
def plus_reputation(request):
    try:
        address_uid, requester_uid = extract_access_data(request)
    except _JWTError as token_error:
        if token_error.args[0] == 'invalid JWT format':
            return JsonResponse({'error': 'INVALID_TOKEN'}, status=400)
        else:
            return JsonResponse({'error': 'EXPIRED_TOKEN'}, status=400)
    if requester_uid == address_uid:
        return JsonResponse({'error': 'REQUESTER_IS_ADDRESSER'}, status=400)
    try:
        new_reputation = __change_rep_algorithm(address_uid, requester_uid, True)
    except ValueError as exception:
        return JsonResponse({'error': exception.args[0]})
    except NotPlayedSameError as exception:
        return JsonResponse({'error': exception.__str__()})
    return JsonResponse({'new_reputation': new_reputation})


@extend_schema(
    request=serializers.MinusReputationRequestSerializer,
    responses={status.HTTP_200_OK: None,
               status.HTTP_400_BAD_REQUEST: serializers.MinusReputationRequestSerializer,
               status.HTTP_403_FORBIDDEN: serializers.MinusReputationRequestSerializer},
    description="Метод для уменьшения репутации пользователя другим пользователем.",
    tags=["Users"],
)
@api_view(['POST'])
def minus_reputation(request):
    try:
        address_uid, requester_uid = extract_access_data(request)
    except _JWTError as token_error:
        if token_error.args[0] == 'invalid JWT format':
            return JsonResponse({'error': 'INVALID_TOKEN'}, status=400)
        else:
            return JsonResponse({'error': 'EXPIRED_TOKEN'}, status=400)
    if requester_uid == address_uid:
        return JsonResponse({'error': 'REQUESTER_IS_ADDRESSER'}, status=400)
    try:
        new_reputation = __change_rep_algorithm(address_uid, requester_uid, False)
    except ValueError as exception:
        return JsonResponse({'error': exception.args[0]}, status=400)
    return JsonResponse({'new_reputation': new_reputation})


@extend_schema(
    request=serializers.BanRequestSerializer,
    responses={status.HTTP_204_NO_CONTENT: None,
               status.HTTP_400_BAD_REQUEST: serializers.BanRequestSerializer,
               status.HTTP_403_FORBIDDEN: serializers.BanRequestSerializer},
    description="Метод для блокировки пользователя другим пользователем.",
    tags=["Users"],
)
@api_view(['POST'])
def ban(request):
    try:
        address_uid, requester_uid = extract_access_data(request)
    except _JWTError as token_error:
        if token_error.args[0] == 'invalid JWT format':
            return JsonResponse({'error': 'INVALID_TOKEN'}, status=400)
        else:
            return JsonResponse({'error': 'EXPIRED_TOKEN'}, status=400)
    if requester_uid == address_uid:
        return JsonResponse({'error': 'REQUESTER_IS_ADDRESSER'}, status=400)
    is_admin = settings.database.child(settings.USERS_TABLE).child(requester_uid).child('user_data').child(
        'is_admin').get().val()
    if not is_admin:
        return JsonResponse({'error': 'ACCESS_DENIED'}, status=400)
    is_banned = settings.database.child(settings.USERS_TABLE).child(address_uid).child('user_data').child(
        'is_banned').get().val()
    if is_banned:
        return JsonResponse({'error': 'ALREADY_BANNED'}, status=400)
    else:
        settings.database.child(settings.USERS_TABLE).child(address_uid).child('user_data').child('is_banned').set(True)
    return Response(status=204)


@extend_schema(
    request=serializers.UnbanRequestSerializer,
    responses={status.HTTP_204_NO_CONTENT: None,
               status.HTTP_400_BAD_REQUEST: serializers.UnbanRequestSerializer,
               status.HTTP_403_FORBIDDEN: serializers.UnbanRequestSerializer},
    description="Метод для разблокировки пользователя другим пользователем.",
    tags=["Users"],
)
@api_view(['POST'])
def unban(request):
    try:
        address_uid, requester_uid = extract_access_data(request)
    except _JWTError as token_error:
        if token_error.args[0] == 'invalid JWT format':
            return JsonResponse({'error': 'INVALID_TOKEN'}, status=400)
        else:
            return JsonResponse({'error': 'EXPIRED_TOKEN'}, status=400)
    if requester_uid == address_uid:
        return JsonResponse({'error': 'REQUESTER_IS_ADDRESSER'})
    is_admin = settings.database.child(settings.USERS_TABLE).child(requester_uid).child('user_data').child(
        'is_admin').get().val()
    if not is_admin:
        return JsonResponse({'error': 'ACCESS_DENIED'}, status=400)
    is_banned = settings.database.child(settings.USERS_TABLE).child(address_uid).child('user_data').child(
        'is_banned').get().val()
    if not is_banned:
        return JsonResponse({'error': 'ALREADY_UNBANNED'}, status=400)
    else:
        settings.database.child(settings.USERS_TABLE).child(address_uid).child('user_data').child('is_banned').set(False)
    return Response(status=204)


def extract_access_data(request):
    body_unicode = request.body.decode('utf-8')
    body_data = json.loads(body_unicode)
    requested_nickname = body_data['requestedNickname']
    id_token = body_data['idToken']

    address_user = settings.database.child(settings.USERS_TABLE).order_by_child('nickname').equal_to(
        requested_nickname).get()
    address_uid = address_user.val().popitem()[0]

    decoded_token = settings.auth.verify_id_token(id_token)
    requester_uid = decoded_token['user_id']
    return address_uid, requester_uid


def __change_rep_algorithm(address_uid, requester_uid, plused):
    if plused:
        opposite_modifier = 1
        changing_name, opposite_name = 'plused', 'minused'
    else:
        opposite_modifier = -1
        changing_name, opposite_name = 'minused', 'plused'
    changes_list = settings.database.child(settings.REPUTATION_TABLE).child(address_uid).child(changing_name).get().val()
    opposite_changes_list = settings.database.child(settings.REPUTATION_TABLE).child(address_uid).child(opposite_name).get().val()
    if not changes_list:
        changes_list = {}
    if not opposite_changes_list:
        opposite_changes_list = {}
    if dict(changes_list).keys().__contains__(requester_uid):
        raise ValueError('ALREADY_CHANGED')
    user_sessions = settings.database.child('users').child(requester_uid).child('user_data').child('played_sessions').get().val()
    address_name = settings.database.child('users').child(address_uid).child('login').get().val()
    print(user_sessions)
    print(requester_uid)
    played_in_same_session = False
    if user_sessions:
        for session_id in user_sessions:
            current_players: list = settings.database.child('sessions').child(session_id).child('players').get().val()
            if (current_players.__contains__(address_name)):
                played_in_same_session = True
                break
    if not played_in_same_session:
        raise NotPlayedSameError


    address_reputation = settings.database.child(settings.USERS_TABLE).child(address_uid).child('user_data').child("reputation").get().val()
    reputation = settings.database.child(settings.USERS_TABLE).child(requester_uid).child('user_data').child("reputation").get().val()

    changes_list[requester_uid] = (math.atan(0.1) * reputation / math.pi) + 0.5
    address_reputation += opposite_modifier * changes_list[requester_uid]

    if dict(opposite_changes_list).__contains__(requester_uid):
        address_reputation += opposite_modifier * opposite_changes_list[requester_uid]
        opposite_changes_list.pop(requester_uid)
    settings.database.child(settings.REPUTATION_TABLE).child(address_uid).child(opposite_name).set(opposite_changes_list)
    settings.database.child(settings.REPUTATION_TABLE).child(address_uid).child(changing_name).set(changes_list)
    settings.database.child(settings.USERS_TABLE).child(address_uid).child('user_data').child('reputation').set(address_reputation)
    return address_reputation


def extract_http_error_message(exception_text):
    message_index = exception_text.find('"message": ') + 12
    message = ""
    while exception_text[message_index] != '"':
        message += exception_text[message_index]
        message_index += 1
    return message

