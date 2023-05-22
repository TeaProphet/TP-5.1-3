import json
from datetime import datetime
import firebase
import math
from django.core import serializers
from django.http import JsonResponse
from rest_framework.decorators import api_view
from rest_framework.exceptions import ValidationError
from rest_framework.response import Response
from onboard import models

config = json.load(open('onboard/onboard_config.json'))
app = firebase.initialize_app(config)
auth = app.auth()
database = app.database()


@api_view(['POST'])
def register(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        user = models.UserSerializer(data=body_data)
        if not user.is_valid():
            raise ValidationError
        user = user.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'})
    try:
        user.register_firebase(database, auth)
    except ValueError as exception:
        return JsonResponse({'error': str(exception)})
    return JsonResponse({'idToken': user.idToken})


@api_view(['POST'])
def credentials_authorize(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        user = models.UserSerializer(data=body_data)
        if not user.is_valid():
            raise ValidationError
        user = user.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'})
    try:
        user.auth_firebase_credentials(database)
    except ValueError:
        return JsonResponse({'error': 'WRONG_CREDENTIALS'})
    return JsonResponse({'idToken': user.idToken})


@api_view(['POST'])
def token_authorize(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        user = models.UserSerializer(data=body_data)
        if not user.is_valid():
            raise ValidationError
        user = user.save()
    except ValidationError:
        return JsonResponse({'error': 'INVALID_CREDENTIALS'})
    try:
        user.auth_firebase_token(database)
    except ValueError:
        return JsonResponse({'error': 'WRONG_CREDENTIALS'})
    data = serializers.serialize('json', [user, ])
    struct = json.loads(data)
    del struct[0]["fields"]["password"]
    return JsonResponse(struct[0]["fields"], safe=False)


@api_view(['POST'])
def create_session(request):
    body_unicode = request.body.decode('utf-8')
    body_data = json.loads(body_unicode)
    session_id = 0
    try:
        all_sessions = database.child("sessions").get().each()
        session_id = len(all_sessions)
    except TypeError:
        pass

    try:
        id_token = body_data.get('idToken')
        name = body_data.get('name')
        city_address = body_data.get('city_address')
        date_time = body_data.get('date_time')
        players_max = body_data.get('players_max')
        datetime.strptime(date_time, '%Y.%m.%d %H:%M')
    except ValueError:
        return JsonResponse({"error": "INVALID_DATA"})

    try:
        login = __get_user_token(id_token).key()
    except Exception as e:
        return JsonResponse({"error": "PERMISSION_DENIED"})
    session_info = {
        "name": name,
        "city_address": city_address,
        "date_time": date_time,
        "players_max": players_max,
        "players": [login]
    }


    database.child("sessions").child(session_id).set(session_info)
    user_sessions = database.child("users").child(login).child("played_sessions").get().val()
    if user_sessions:
        user_sessions += [session_id]
    else:
        user_sessions = [session_id]
    database.child("users").child(login).child("played_sessions").set(user_sessions)
    return Response(status=204)


@api_view(['POST'])
def get_profile_info(request):
    body_unicode = request.body.decode('utf-8')
    body_data = json.loads(body_unicode)
    id_token = body_data.get('searchedLogin')
    user = __get_user(id_token)
    user_info = dict(user.val())
    del user_info['idToken']
    del user_info['password']

    return Response(status=204)


@api_view(['POST'])
def get_session_info(request):
    body_unicode = request.body.decode('utf-8')
    body_data = json.loads(body_unicode)
    session_id = body_data.get('sessionId')
    session_info = dict(database.child('sessions').child(session_id).get().val())
    players_info = {}
    for player_login in session_info.get('players'):
        players_info[player_login] = {'reputation': dict(__get_user(player_login).val()).get('reputation')}
    session_info['players'] = players_info
    return JsonResponse(session_info)


@api_view(['POST'])
def change_profile(request):
    body_unicode = request.body.decode('utf-8')
    body_data = json.loads(body_unicode)
    try:
        id_token = body_data.get('idToken')
        age = body_data.get('age')
        games = body_data.get('games')
        vk = body_data.get('vk')
        tg = body_data.get('tg')
    except Exception:
        return JsonResponse('INVALID_DATA')
    login = __get_user_token(id_token).key()
    database.child("users").child(login).child("age").set(age)
    database.child("users").child(login).child("games").set(games)
    database.child("users").child(login).child("vk").set(vk)
    database.child("users").child(login).child("tg").set(tg)
    try:
        new_password = body_data.get('password')
        database.child("users").child(login).child("password").set(new_password)
    except Exception:
        pass
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
    already_plused = database.child("reputation_data").child(address_login).child("plused_reputation").get().val()
    requester_login = __get_user_token(id_token).key()
    if requester_login == address_login:
        return JsonResponse({"error": "REQUESTER_IS_ADDRESSER"})
    if already_plused:
        already_plused = dict(already_plused)
        if dict(already_plused).keys().__contains__(requester_login):
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
    already_minused = database.child("reputation_data").child(address_login).child("minused_reputation").get().val()
    requester_login = __get_user_token(id_token).key()
    if requester_login == address_login:
        return JsonResponse({"error": "REQUESTER_IS_ADDRESSER"})
    if already_minused:
        already_minused = dict(already_minused)
        if dict(already_minused).keys().__contains__(requester_login):
            return JsonResponse({"error": "ALREADY_CHANGED"})
        else:
            address_reputation = __minus_algorithm(address_login, requester_login, already_minused)
            return JsonResponse({"new_reputation": address_reputation})
    else:
        address_reputation = __minus_algorithm(address_login, requester_login, {})
        return JsonResponse({"new_reputation": address_reputation})





def __get_user(login):
    user = database.child("users").child(login).get()
    return user


def __get_user_token(id_token):
    find_response = database.child("users").order_by_child("idToken").equal_to(id_token).get().each()
    return find_response[0]

def __plus_algorithm(address_login, requester_login, already_plused):
    address_reputation = database.child("users").child(address_login).child("reputation").get().val()
    reputation = database.child("users").child(requester_login).child("reputation").get().val()
    address_reputation += (math.atan(0.1) * reputation / math.pi) + 0.5
    already_plused[requester_login] = (math.atan(0.1) * reputation / math.pi) + 0.5
    already_minused = database.child("reputation_data").child(address_login).child("minused_reputation").get().val()
    if already_minused:
        already_minused = dict(already_minused)
        if already_minused.__contains__(requester_login):
            address_reputation += already_minused[requester_login]
            already_minused.pop(requester_login)
    database.child("reputation_data").child(address_login).child("minused_reputation").set(already_minused)
    database.child("reputation_data").child(address_login).child("plused_reputation").set(already_plused)
    database.child("users").child(address_login).child("reputation").set(address_reputation)
    return address_reputation

def __minus_algorithm(address_login, requester_login, already_minused):
    address_reputation = database.child("users").child(address_login).child("reputation").get().val()
    reputation = database.child("users").child(requester_login).child("reputation").get().val()
    address_reputation -= (math.atan(0.1) * reputation / math.pi) + 0.5
    already_minused[requester_login] = (math.atan(0.1) * reputation / math.pi) + 0.5
    already_plused = database.child("reputation_data").child(address_login).child("plused_reputation").get().val()
    if already_plused:
        already_plused = dict(already_plused)
        if already_plused.__contains__(requester_login):
            address_reputation -= already_plused[requester_login]
            already_plused.pop(requester_login)
    database.child("reputation_data").child(address_login).child("minused_reputation").set(already_minused)
    database.child("reputation_data").child(address_login).child("plused_reputation").set(already_plused)
    database.child("users").child(address_login).child("reputation").set(address_reputation)
    return address_reputation
