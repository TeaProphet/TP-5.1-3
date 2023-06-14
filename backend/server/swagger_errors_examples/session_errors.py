from drf_spectacular.utils import OpenApiExample

session_not_found = OpenApiExample(
	'Игровая сессия не найдена',
	summary="Ошибка: игровая сессия не найдена",
	description="Ошибка, возникающая при введении несуществующего идентификатора игровой сессии. Возвращает код ошибки error.",
	value={
		'error': 'INVALID_SESSION_ID'
	},
	response_only=True,
	status_codes=[404]
)
already_joined = OpenApiExample(
	'Вы уже участник игровой сессии',
	summary="Ошибка: вы уже участник игровой сессии",
	description="Ошибка, возникающая при попытке стать членом игровой сессии в которой пользователь уже состоит. Возвращает код ошибки error.",
	value={
		'error': 'INVALID_SESSION_ID'
	},
	response_only=True,
	status_codes=[400]
)
already_left = OpenApiExample(
	'Вы уже не являетесь участником игровой сессии',
	summary="Ошибка: вы не являетесь участником игровой сессии",
	description="Ошибка, возникающая при попытке покинуть игровую сессию участником которой вы не являетесь. Возвращает код ошибки error.",
	value={
		'error': 'ALREADY_LEFT'
	},
	response_only=True,
	status_codes=[400]
)
