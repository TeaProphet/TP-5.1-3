import json
from datetime import datetime

import firebase
from django.core import serializers
from django.http import JsonResponse
from rest_framework.decorators import api_view
from rest_framework.exceptions import ValidationError
from rest_framework.response import Response

from onboard import models

# Create your views here.
config = json.load(open('onboard/onboard_config.json'))
app = firebase.initialize_app(config)
auth = app.auth()
database = app.database()


@api_view(['POST'])
def register(request):
    try:
        body_unicode = request.body.decode('utf-8')
        body_data = json.loads(body_unicode)
        print(body_data)
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
    print(__get_user(user.login))
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
        user.auth_firebase_token(database, auth)
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
        last_session_id = database.child("sessions").order_by_child("sessionId").get().each()[0].key()
        session_id = last_session_id + 1
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
        print(e)
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


def __get_user(login):
    user = database.child("users").child(login).get()
    return dict(user.val())

def __get_user_token(id_token):
    find_response = database.child("users").order_by_child("idToken").equal_to(id_token).get().each()
    return find_response[0]

