from drf_spectacular.utils import OpenApiExample


invalid_credentials = OpenApiExample(
	'Неверные аутентификационнные данные',
	summary="Ошибка: неверные аутентификацоннные данные",
	description="Ошибка, возникающая при введении неверных данных для регистрации/авторизации. Возвращает код ошибки error.",
	value={
		'error': 'INVALID_CREDENTIALS'
	},
	response_only=True,
	status_codes=[400]
)
invalid_email = OpenApiExample(
	'Некорректная электронная почта',
	summary="Ошибка: некорректная электронная почта",
	description="Ошибка, возникающая при введении некорректной электронной почты. Возвращает код ошибки error.",
	value={
		'error': 'INVALID_EMAIL'
	},
	response_only=True,
	status_codes=[400]
)
email_not_exist = OpenApiExample(
	'Электронная почта не существует',
	summary="Ошибка: несуществующий логин",
	description="Ошибка, возникающая при введении несуществующего логина. Возвращает код ошибки error.",
	value={
		'error': 'EMAIL_NOT_FOUND'
	},
	response_only=True,
	status_codes=[400]
)
weak_password = OpenApiExample(
	'Слабый пароль',
	summary="Ошибка: слабый пароль",
	description="Ошибка, возникающая при введении пароля длинной менее 6 символов. Возвращает код ошибки error.",
	value={
		'error': 'WEAK_PASSWORD : Password should be at least 6 characters'
	},
	response_only=True,
	status_codes=[400]
)
nickname_exists = OpenApiExample(
	'Никнейм уже существует',
	summary="Ошибка: никнейм уже существует",
	description="Ошибка, возникающая при введении при регистрации никнейма который уже сущесвует. Возвращает код ошибки error.",
	value={
		'error': 'NICKNAME_EXISTS'
	},
	response_only=True,
	status_codes=[400]
)
email_exists = OpenApiExample(
	'Почта уже зарегистрирована',
	summary="Ошибка: электронная почта зарегистрирована",
	description="Ошибка, возникающая при введении электронной почты, которая уже зарегистрирована. Возвращает код ошибки error.",
	value={
		'error': 'EMAIL_EXISTS'
	},
	response_only=True,
	status_codes=[400]
)
already_plused = OpenApiExample(
	'Репутация уже увеличена',
	summary="Ошибка: репутация уже увеличена",
	description="Ошибка, возникающая при попытке увеличить репутацию пользователю, которому вы уже увеличивали репутацию. Возвращает код ошибки error.",
	value={
		'error': 'ALREADY_PLUSED'
	},
	response_only=True,
	status_codes=[400]
)
already_minused = OpenApiExample(
	'Репутация уже уменьшена',
	summary="Ошибка: репутация уже уменьшена",
	description="Ошибка, возникающая при попытке уменьшить репутацию пользователю, которому вы уже уменьшали репутацию. Возвращает код ошибки error.",
	value={
		'error': 'ALREADY_MINUSED'
	},
	response_only=True,
	status_codes=[400]
)
already_banned = OpenApiExample(
	'Пользователь уже заблокирован',
	summary="Ошибка: пользователь уже заблокирован",
	description="Ошибка, возникающая при попытке заблокировать пользователю, который уже заблокирован. Возвращает код ошибки error.",
	value={
		'error': 'ALREADY_BANNED'
	},
	response_only=True,
	status_codes=[400]
)
already_unbanned = OpenApiExample(
	'Пользователь уже разблокирован',
	summary="Ошибка: пользователь уже разблокирован",
	description="Ошибка, возникающая при попытке разблокировать пользователю, который уже разблокирован. Возвращает код ошибки error.",
	value={
		'error': 'ALREADY_UNBANNED'
	},
	response_only=True,
	status_codes=[400]
)
invalid_token = OpenApiExample(
	'Некорректный токен',
	summary="Ошибка: некорректный токен",
	description="Ошибка, возникающая при неверно введённом токене. Возвращает код ошибки error.",
	value={
		'error': 'INVALID_TOKEN'
	},
	response_only=True,
	status_codes=[400]
)
expired_token = OpenApiExample(
	'Истёкший токен',
	summary="Ошибка: истёкший токен",
	description="Ошибка, возникающая при введении истёкшего токена. Возвращает код ошибки error.",
	value={
		'error': 'EXPIRED_TOKEN'
	},
	response_only=True,
	status_codes=[400]
)
access_denied = OpenApiExample(
	'Доступ запрещён',
	summary="Ошибка: доступ запрещён",
	description="Ошибка, возникающая при попытке осуществить действие, для которого у пользователя нет полномочий. Возвращает код ошибки error.",
	value={
		'error': 'ACCESS_DENIED'
	},
	response_only=True,
	status_codes=[403]
)

