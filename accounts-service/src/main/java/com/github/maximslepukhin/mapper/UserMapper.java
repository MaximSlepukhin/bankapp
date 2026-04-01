package com.github.maximslepukhin.mapper;

import com.github.maximslepukhin.model.dto.AccountDto;
import com.github.maximslepukhin.model.dto.UserDto;
import com.github.maximslepukhin.model.entity.Account;
import com.github.maximslepukhin.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct-маппер: генерирует реализацию этого интерфейса на этапе компиляции.
 *
 * componentModel = "spring" — сгенерированный класс будет Spring @Component,
 * поэтому его можно инжектить через @Autowired / конструктор как обычный бин.
 *
 * Что генерирует MapStruct под капотом (можно посмотреть в target/generated-sources):
 *
 *   @Component
 *   public class UserMapperImpl implements UserMapper {
 *       public UserDto toDto(User user) {
 *           UserDto dto = new UserDto();
 *           dto.setKeycloakId(user.getKeycloakId());
 *           dto.setLogin(user.getLogin());
 *           ...
 *           dto.setAccounts(accountListToAccountDtoList(user.getAccounts()));
 *           return dto;
 *       }
 *       ...
 *   }
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * User → UserDto.
     *
     * Поля keycloakId / login / name / birthdate маппятся автоматически —
     * имена и типы совпадают, MapStruct не требует аннотаций.
     *
     * Поле accounts (List<Account> → List<AccountDto>) тоже автоматически:
     * MapStruct видит метод toAccountDto(Account) ниже и применяет его к каждому элементу.
     */
    UserDto toDto(User user);

    /**
     * Account → AccountDto.
     *
     * currency: в Account это enum Currency, в AccountDto — String.
     *   expression = "java(...)" — позволяет написать произвольный Java-код.
     *   Здесь вызываем .name() чтобы получить "USD" / "RUB" / "CNY".
     *
     * title: поля с таким именем в Account нет — оно внутри enum Currency.
     *   Снова используем expression для вызова currency.getTitle().
     *
     * value: имена совпадают, тип BigDecimal → BigDecimal, маппится автоматически.
     *
     * exists: в Account такого поля нет вообще — счёт всегда считается открытым.
     *   constant = "true" подставляет литерал напрямую в сгенерированный код.
     */
    @Mapping(target = "currency", expression = "java(account.getCurrency().name())")
    @Mapping(target = "title",    expression = "java(account.getCurrency().getTitle())")
    @Mapping(target = "exists",   constant = "true")
    AccountDto toAccountDto(Account account);
}
