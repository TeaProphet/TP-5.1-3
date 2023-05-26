from onboardProject import settings


def get_user(login):
    user = settings.database.child("users").child(login).get()
    return user


def get_user_token(id_token):
    find_response = settings.database.child("users").order_by_child("idToken").equal_to(id_token).get().each()
    return find_response[0]