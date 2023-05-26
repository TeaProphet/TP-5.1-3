import json
import math
from django.http import JsonResponse
from rest_framework.decorators import api_view
from rest_framework.exceptions import ValidationError
from rest_framework.response import Response
from onboardProject import settings
from users import models
from firebase_tools.users_tools import get_user, get_user_token


@api_view(['POST'])
def register(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        user = models.UserCredentialsSerializer(data=body_data)
        if not user.is_valid():
            raise ValidationError
        user = user.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'})
    try:
        user.register_firebase()
    except ValueError as exception:
        return JsonResponse({'error': str(exception)})
    return JsonResponse({'idToken': user.idToken})


@api_view(['POST'])
def credentials_authorize(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        user = models.UserCredentialsSerializer(data=body_data)
        if not user.is_valid():
            raise ValidationError
        user = user.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'})
    try:
        user.auth_firebase_credentials()
    except ValueError:
        return JsonResponse({'error': 'WRONG_CREDENTIALS'})
    return JsonResponse({'idToken': user.idToken})


@api_view(['POST'])
def token_authorize(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        user = models.UserCredentialsSerializer(data=body_data)
        if not user.is_valid():
            raise ValidationError
        user = user.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'})
    try:
        user.auth_firebase_token()
    except ValueError:
        return JsonResponse({'error': 'WRONG_CREDENTIALS'})
    data = models.UserCredentialsSerializer(user).data
    data.pop('idToken')
    return JsonResponse(data, safe=False)


@api_view(['POST'])
def get_profile_info(request):
    body_unicode = request.body.decode('utf-8')
    body_data = json.loads(body_unicode)
    login = body_data.get('searchedLogin')
    user_data = get_user(login)
    return JsonResponse(models.UserDataSerializer(user_data).data)

@api_view(['POST'])
def change_profile(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        user = models.UserCredentialsSerializer(data=body_data)
        if not user.is_valid():
            raise ValidationError
        user = user.save()
        user_data = models.UserDataSerializer(data=body_data)
        if not user_data.is_valid():
            raise ValidationError
        user_data = user_data.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_DATA'})
    user.user_data = user_data
    user.login = get_user_token(user.idToken).key()
    user.update_user_data()
    return Response(status=204)


@api_view(['POST'])
def plus_reputation(request):
    body_unicode = request.body.decode('utf-8')
    body_data = json.loads(body_unicode)
    try:
        id_token = body_data.get('idToken')
        address_login = body_data.get('address_login')
    except Exception:
        return JsonResponse({"error": "WRONG_DATA"})
    already_plused = settings.database.child("reputation_data").child(address_login).child("plused_reputation").get().val()
    requester_login = get_user_token(id_token).key()
    if requester_login == address_login:
        return JsonResponse({"error": "REQUESTER_IS_ADDRESSER"})
    if already_plused:
        already_plused = dict(already_plused)
        if dict(already_plused).keys().contains(requester_login):
            return JsonResponse({"error": "ALREADY_CHANGED"})
        else:
            address_reputation = __plus_algorithm(address_login, requester_login, already_plused)
            return JsonResponse({"new_reputation": address_reputation})
    else:
        address_reputation = __plus_algorithm(address_login, requester_login, {})
        return JsonResponse({"new_reputation": address_reputation})


@api_view(['POST'])
def minus_reputation(request):
    body_unicode = request.body.decode('utf-8')
    body_data = json.loads(body_unicode)
    try:
        id_token = body_data.get('idToken')
        address_login = body_data.get('address_login')
    except Exception:
        return JsonResponse({"error": "WRONG_DATA"})
    already_minused = settings.database.child("reputation_data").child(address_login).child("minused_reputation").get().val()
    requester_login = get_user_token(id_token).key()
    if requester_login == address_login:
        return JsonResponse({"error": "REQUESTER_IS_ADDRESSER"})
    if already_minused:
        already_minused = dict(already_minused)
        if dict(already_minused).keys().contains(requester_login):
            return JsonResponse({"error": "ALREADY_CHANGED"})
        else:
            address_reputation = __minus_algorithm(address_login, requester_login, already_minused)
            return JsonResponse({"new_reputation": address_reputation})
    else:
        address_reputation = __minus_algorithm(address_login, requester_login, {})
        return JsonResponse({"new_reputation": address_reputation})

def __plus_algorithm(address_login, requester_login, already_plused):
    address_reputation = settings.database.child("users").child(address_login).child("reputation").get().val()
    reputation = settings.database.child("users").child(requester_login).child("reputation").get().val()
    address_reputation += (math.atan(0.1) * reputation / math.pi) + 0.5
    already_plused[requester_login] = (math.atan(0.1) * reputation / math.pi) + 0.5
    already_minused = settings.database.child("reputation_data").child(address_login).child("minused_reputation").get().val()
    if already_minused:
        already_minused = dict(already_minused)
        if already_minused.contains(requester_login):
            address_reputation += already_minused[requester_login]
            already_minused.pop(requester_login)
    settings.database.child("reputation_data").child(address_login).child("minused_reputation").set(already_minused)
    settings.database.child("reputation_data").child(address_login).child("plused_reputation").set(already_plused)
    settings.database.child("users").child(address_login).child("reputation").set(address_reputation)
    return address_reputation

def __minus_algorithm(address_login, requester_login, already_minused):
    address_reputation = settings.database.child("users").child(address_login).child("reputation").get().val()
    reputation = settings.database.child("users").child(requester_login).child("reputation").get().val()
    address_reputation -= (math.atan(0.1) * reputation / math.pi) + 0.5
    already_minused[requester_login] = (math.atan(0.1) * reputation / math.pi) + 0.5
    already_plused = settings.database.child("reputation_data").child(address_login).child("plused_reputation").get().val()
    if already_plused:
        already_plused = dict(already_plused)
        if already_plused.contains(requester_login):
            address_reputation -= already_plused[requester_login]
            already_plused.pop(requester_login)
    settings.database.child("reputation_data").child(address_login).child("minused_reputation").set(already_minused)
    settings.database.child("reputation_data").child(address_login).child("plused_reputation").set(already_plused)
    settings.database.child("users").child(address_login).child("reputation").set(address_reputation)
    return address_reputation
